package jp.ka.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.http.Header;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class HttpUtils implements ApplicationListener<ContextRefreshedEvent> {

  private static ApplicationContext applicationContext;

  private static String U2Domain;
  @Value("${u2.domain}")
  public void setDomain(String domain) {
    this.U2Domain = domain;
  }

  private static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

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

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    this.applicationContext = event.getApplicationContext();
  }

  // 创建SSL安全连接
  private static SSLConnectionSocketFactory createSSLConnSocketFactory() throws HttpException {
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
      applicationContext.getBean(Receiver.class).sendMsg(Config.uid, Text.REQUEST_ERROR, "md", -1);
      throw new HttpException(501, e.getMessage());
    }
  }

  private static Response req(HttpRequestBase request) throws HttpException {
    CloseableHttpClient cli = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
    HttpClientContext context = HttpClientContext.create();

    try {
      HttpResponse response = cli.execute(request, context);
      for (Cookie cookie : context.getCookieStore().getCookies()) {
        Config.session.put(cookie.getName(), cookie.getValue());
      }

      byte[] result = EntityUtils.toByteArray(response.getEntity());
      int code = response.getStatusLine().getStatusCode();
      log.info("[Session] {}", Config.session);
      log.info("[Http Response <"+ code +"> <"+ request.getURI() +">]\n\n{}\n", response);
      ParseHTML html = isHTML(result);
      if (code == 200 && Objects.nonNull(html) && html.getIs()) isLogin(html.getHtml());
      // log.info("[Http Response Body <"+ code +"> <"+ request.getURI() +">]\n\n{}\n", new String(result));
      if (Objects.nonNull(html) && !html.getIs()) log.info("[Http Response Body <"+ code +"> <"+ request.getURI() +">]\n\n{}\n", new String(result));
      if (code >= 400 && code < 500) {
        applicationContext.getBean(Receiver.class).sendMsg(Config.uid, Text.NOT_FOUND, "md", -1);
        throw new HttpException(code, result.toString());
      }
      if (code >= 500) {
        applicationContext.getBean(Receiver.class).sendMsg(Config.uid, Text.U2_SERVER_ERROR, "md", -1);
        throw new HttpException(code, result.toString());
      }
      return new Response(response, code, result, html);
    } catch (IOException e) {
      log.error("[Request Exception "+ request.getURI() +"]", e);
      applicationContext.getBean(Receiver.class).sendMsg(Config.uid, Text.REQUEST_ERROR, "md", -1);
      throw new HttpException(502, e.getMessage());
    } finally {
      request.releaseConnection();
    }
  }

  public static RespGet get(String uri) throws HttpException {
    HttpGet get = new HttpGet(U2Domain + uri);
    get.addHeader("accept", "*/*");
    get.addHeader("user-agent", UA);
    if (!Config.session.isEmpty()) get.addHeader("cookie", cookieToString());

    Response resp = req(get);
    if (Objects.nonNull(resp.getHtml())) {
      isLogin(resp.html.getHtml());
      if (resp.html.getIs()) return new RespGet(resp.getCode(), null, resp.html.getHtml());
    }

    return new RespGet(resp.getCode(), resp.getResult(), null);
  }
  public static InputStream getPic(String uri) throws HttpException {
    HttpGet get = new HttpGet(U2Domain + uri);
    get.addHeader("accept", "*/*");
    get.addHeader("user-agent", UA);
    if (!Config.session.isEmpty()) get.addHeader("cookie", cookieToString());

    return new ByteArrayInputStream(req(get).getResult());
  }

  private static RespPost post(String uri, String mediaType, HttpEntity entity) throws HttpException {
    HttpPost post = new HttpPost(U2Domain + uri);
    post.addHeader("content-type", mediaType);
    post.addHeader("accept", "*/*");
    post.addHeader("user-agent", UA);
    if (!Config.session.isEmpty()) post.addHeader("cookie", cookieToString());
    post.setEntity(entity);

    Response resp = req(post);

    if (resp.getCode() == 200) {
      if (resp.html.getIs()) return new RespPost(resp.getCode(), null, resp.html.getHtml());
      else return new RespPost(resp.getCode(), new Gson().fromJson(new String(resp.getResult()), new TypeToken<Map<String, String>>() {}.getType()), null);
    } else if (resp.getCode() == 302) {
      String redirectURL = resp.getResponse().getFirstHeader("location").getValue();
      if (!redirectURL.equals("")) post(redirectURL, mediaType, entity);
    }

    return new RespPost(resp.getCode(), null, null);
  }

  // public static Map<String, String> postJSON(String path, String json) throws HttpException {
  //   StringEntity entity = new StringEntity(json, Charsets.UTF_8);
  //   return post(path, "application/json", entity);
  // }

  public static RespPost postForm(String path, List<NameValuePair> parametersBody) throws HttpException {
    HttpEntity entity = new UrlEncodedFormEntity(parametersBody, Charsets.UTF_8);
    return post(path, "application/x-www-form-urlencoded", entity);
  }

  private static ParseHTML isHTML(byte[] result) {
    if (result.length == 0) return null;
    Document html = Jsoup.parse(new String(result), "UTF-8");
    Elements title = html.getElementsByTag("title");
    if (title.size() == 0) return new ParseHTML(false, null);
    else return new ParseHTML(true, html);
  }

  private static void isLogin(Document html) throws HttpException {
    Element title = html.getElementsByTag("title").get(0);
    if (title.text().equals("Access Point :: U2")) {
      applicationContext.getBean(Receiver.class).sendMsg(Config.uid, Text.LOGIN_EXPIRE, "md", -1);
      Config.uid = null;
      Config.step = null;
      Config.session.clear();
      throw new HttpException(403, "not login");
    }
  }

  private static String cookieToString() {
    String tmp = "";

    for (Map.Entry<String, String> entry : Config.session.entrySet()) {
      if (tmp.equals("")) {
        tmp += entry.getKey() + "=" + entry.getValue();
      } else {
        tmp += "; " + entry.getKey() + "=" + entry.getValue();
      }
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
