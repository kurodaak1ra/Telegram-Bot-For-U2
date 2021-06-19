package jp.ka.config;

import jp.ka.command.CommandTools;
import jp.ka.controller.CallbackResolver;
import jp.ka.controller.CommandResolver;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.Store;
import lombok.extern.slf4j.Slf4j;
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

  public static Long uid;
  @Value("${user.id}")
  public void setUID(Long uid) {
    this.uid = uid;
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
    if (!U2Cookie.equals("")) {
      if (Objects.isNull(uid)) {
        log.info("手动设置了 Cookie 必须再手动设置 Telegram Number UID");
        System.exit(0);
      }
      for (String cookie : U2Cookie.split(";")) {
        String[] split = cookie.split("=");
        if (split.length == 2) HttpUtils.session.put(split[0], split[1]);
      }
      CommandTools.userInfo(uid);
      CommonUtils.pushServiceStart();
    }
    log.info("[Inject Cookie] {}", HttpUtils.session);
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext context = event.getApplicationContext();
    Store.context = context;
    context.getBean(CommandResolver.class).initCommandMap(context);
    context.getBean(CallbackResolver.class).initCallbackMap(context);
  }

}
