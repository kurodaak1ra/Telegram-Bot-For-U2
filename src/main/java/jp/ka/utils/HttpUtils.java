package jp.ka.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jp.ka.bean.RespGet;
import jp.ka.bean.RespPost;
import jp.ka.bean.config.U2;
import jp.ka.bean.config.User;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.variable.MsgTpl;
import jp.ka.variable.Store;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HttpUtils {

  private static U2 u2;
  private static User user;

  @Autowired
  public void setU2(U2 u2) {
    this.u2 = u2;
  }
  @Autowired
  public void setUser(User user) {
    this.user = user;
  }

  public static Map<String, String> session = new HashMap<>();

  public static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:89.0) Gecko/20100101 Firefox/89.0";

  private static PoolingHttpClientConnectionManager connMgr;
  private static final int MAX_TIMEOUT = 7000;
  private static RequestConfig requestConfig;

  static {
    // 设置连接池
    connMgr = new PoolingHttpClientConnectionManager();
    // 设置连接池大小
    connMgr.setMaxTotal(100);
    connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());

    RequestConfig.Builder configBuilder = RequestConfig.custom();
    // 设置连接超时
    configBuilder.setConnectTimeout(MAX_TIMEOUT);
    // 设置读取超时
    configBuilder.setSocketTimeout(MAX_TIMEOUT);
    // 设置从连接池获取连接实例的超时
    configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
    // 在提交请求之前 测试连接是否可用
    configBuilder.setStaleConnectionCheckEnabled(true);
    requestConfig = configBuilder.build();
  }

  // 创建SSL安全连接
  private static SSLConnectionSocketFactory createSSLConnSocketFactory(Long gid) throws HttpException {
    try {
      SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
          return true;
        }
      }).build();
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
        @Override
        public boolean verify(String arg0, SSLSession arg1) {
          return true;
        }

        @Override
        public void verify(String host, SSLSocket ssl) throws IOException { }

        @Override
        public void verify(String host, X509Certificate cert) throws SSLException { }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException { }
      });
      return sslsf;
    } catch (GeneralSecurityException e) {
      log.error("[createSSLConnSocketFactory Exception]", e);
      Store.context.getBean(Receiver.class).sendMsg(gid, "md", MsgTpl.REQUEST_ERROR, null);
      throw new HttpException(501, e.getMessage());
    }
  }

  private static Response req(Long gid, HttpRequestBase request) throws HttpException {
    CloseableHttpClient cli = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory(gid)).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
    HttpClientContext context = HttpClientContext.create();

    try {
      HttpResponse response = cli.execute(request, context);
      for (Cookie cookie : context.getCookieStore().getCookies()) {
        session.put(cookie.getName(), cookie.getValue());
      }

      byte[] result = EntityUtils.toByteArray(response.getEntity());
      int code = response.getStatusLine().getStatusCode();
      if (!Pattern.compile("messages\\.php|/promotion\\.php").matcher(request.getURI().toString()).find()) {
        log.info("[Session] {}", session);
        log.info("[{} Response <{}> <{}>]\n\n{}\n", request.getMethod(), code, request.getURI(), response);
      }
      ParseHTML html = isHTML(result);
      if (code == 200 && Objects.nonNull(html) && html.getIs()) isLogin(gid, html.getHtml());
      // log.info("[{} Response Body <{}> <{}>]\n\n{}\n", request.getMethod(), code, request.getURI(), new String(result));
      if (Objects.nonNull(html) && !html.getIs()) log.info("[{} Response Body <{}> <{}>]\n\n{}\n", request.getMethod(), code, request.getURI(), new String(result));
      if (code >= 400 && code < 500) {
        Store.context.getBean(Receiver.class).sendMsg(gid, "md", MsgTpl.NOT_FOUND, null);
        throw new HttpException(code, result.toString());
      }
      if (code >= 500) {
        Store.context.getBean(Receiver.class).sendMsg(gid, "md", MsgTpl.U2_SERVER_ERROR, null);
        throw new HttpException(code, result.toString());
      }
      return new Response(response, code, result, html);
    } catch (IOException e) {
      log.error("[Request Exception "+ request.getURI() +"]", e);
      Store.context.getBean(Receiver.class).sendMsg(gid, "md", MsgTpl.REQUEST_ERROR, null);
      throw new HttpException(502, e.getMessage());
    } finally {
      request.releaseConnection();
    }
  }

  public static RespGet get(Long gid, String uri) throws HttpException {
    HttpGet get = new HttpGet(u2.getDomain() + uri);
    get.addHeader("accept", "*/*");
    get.addHeader("user-agent", UA);
    if (!session.isEmpty()) get.addHeader("cookie", cookieToString());

    Response resp = req(gid, get);
    if (Objects.nonNull(resp.getHtml())) {
      isLogin(gid, resp.html.getHtml());
      if (resp.html.getIs()) return new RespGet(resp.getCode(), null, resp.html.getHtml());
    }

    return new RespGet(resp.getCode(), resp.getResult(), null);
  }
  public static InputStream getPic(Long gid, String uri) throws HttpException {
    HttpGet get = new HttpGet(u2.getDomain() + uri);
    get.addHeader("accept", "*/*");
    get.addHeader("user-agent", UA);
    if (!session.isEmpty()) get.addHeader("cookie", cookieToString());

    return new ByteArrayInputStream(req(gid, get).getResult());
  }

  private static RespPost post(Long gid, String uri, String mediaType, HttpEntity entity) throws HttpException {
    HttpPost post = new HttpPost(u2.getDomain() + uri);
    post.addHeader("content-type", mediaType);
    post.addHeader("accept", "*/*");
    post.addHeader("user-agent", UA);
    if (!session.isEmpty()) post.addHeader("cookie", cookieToString());
    post.setEntity(entity);

    Response resp = req(gid, post);

    if (resp.getCode() == 200) {
      if (resp.html.getIs()) return new RespPost(resp.getCode(), null, resp.html.getHtml());
      else return new RespPost(resp.getCode(), new Gson().fromJson(new String(resp.getResult()), new TypeToken<Map<String, String>>() {}.getType()), null);
    } else if (resp.getCode() == 302) {
      String redirectURL = resp.getResponse().getFirstHeader("location").getValue();
      if (!redirectURL.equals("")) post(gid, redirectURL, mediaType, entity);
    }

    return new RespPost(resp.getCode(), null, null);
  }

  public static RespPost postJSON(Long gid, String path, Map<String, Object> params) throws HttpException {
    StringEntity entity = new StringEntity(new Gson().toJson(params), StandardCharsets.UTF_8);
    return post(gid, path, "application/json", entity);
  }

  public static RespPost postForm(Long gid, String path, List<NameValuePair> parametersBody) throws HttpException {
    HttpEntity entity = new UrlEncodedFormEntity(parametersBody, StandardCharsets.UTF_8);
    return post(gid, path, "application/x-www-form-urlencoded", entity);
  }

  private static ParseHTML isHTML(byte[] result) {
    if (result.length == 0) return null;
    Document html = Jsoup.parse(new String(result), "UTF-8");
    Elements title = html.getElementsByTag("title");
    Elements script = html.getElementsByTag("script");
    if (title.size() != 0 || script.size() != 0) return new ParseHTML(true, html);
    return new ParseHTML(false, null);
  }

  private static void isLogin(Long gid, Document html) throws HttpException {
    Elements title = html.getElementsByTag("title");
    if (title.size() == 0) return;
    if (title.get(0).text().equals("Access Point :: U2")) {
      Store.context.getBean(Receiver.class).sendMsg(gid, "md", MsgTpl.LOGIN_EXPIRE, null);
      Store.STEP = null;
      user.setUid(null);
      session.clear();
      throw new HttpException(403, "not login");
    }
  }

  private static String cookieToString() {
    String tmp = "";

    for (Map.Entry<String, String> entry : session.entrySet()) {
      if (tmp.equals("")) tmp += entry.getKey() + "=" + entry.getValue();
      else tmp += "; " + entry.getKey() + "=" + entry.getValue();
    }

    return tmp;
  }

  @Data
  @AllArgsConstructor
  private static class Response {
    private HttpResponse response;
    private Integer code;
    private byte[] result;
    private ParseHTML html;
  }

  @Data
  @AllArgsConstructor
  private static class ParseHTML {
    private Boolean is;
    private Document html;
  }

}
