package jp.ka.config;

import jp.ka.bean.U2Cookie;
import jp.ka.bean.U2Info;
import jp.ka.command.CommandTools;
import jp.ka.controller.CallbackResolver;
import jp.ka.controller.CommandResolver;
import jp.ka.mapper.U2Mapper;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class Config implements CommandLineRunner, ApplicationListener<ContextRefreshedEvent> {

  @Autowired
  private U2Mapper mapper;

  public static Long id;
  @Value("${user.id}")
  public void setUID(Long id) {
    this.id = id;
  }

  public static String U2Domain;
  @Value("${u2.domain}")
  public void setDomain(String domain) {
    this.U2Domain = domain;
  }

  private static String U2Cookie;
  @Value("${u2.cookie}")
  public void setCookieValue(String cookie) {
    this.U2Cookie = cookie;
  }

  public static String phantomjs;
  @Value("${phantomjs}")
  public void setPhantomjs(String phantomjs) {
    this.phantomjs = phantomjs;
  }

  @Override // 初始化数据
  public void run(String... args) throws Exception {
    U2Info info = mapper.queryInfo();
    U2Cookie[] cookies = mapper.queryCookies();
    if (Objects.nonNull(info) && Objects.nonNull(cookies)) {
      id = info.getId();
      U2.uid = info.getUid().toString();
      U2.pageKey = info.getPageKey();
      U2.passKey = info.getPassKey();
      for (U2Cookie cookie : cookies) {
        HttpUtils.session.put(cookie.getK(), cookie.getV());
      }
      CommandTools.userInfo(id);
      log.info("[Login Data From SQL]");
      return;
    }

    if (!U2Cookie.equals("")) {
      if (Objects.isNull(id)) {
        log.info("手动设置了 Cookie 必须再手动设置 Telegram Number UID");
        System.exit(0);
      }
      for (String cookie : U2Cookie.split(";")) {
        String[] split = cookie.split("=");
        if (split.length == 2) {
          HttpUtils.session.put(split[0], split[1]);
          mapper.insertCookies(new U2Cookie(split[0], split[1]));
        }
      }
      CommandTools.loginSucc();
      log.info("[Inject Cookie] {}", HttpUtils.session);
    }
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext context = event.getApplicationContext();
    Store.context = context;
    context.getBean(CommandResolver.class).initCommandMap(context);
    context.getBean(CallbackResolver.class).initCallbackMap(context);
  }

}
