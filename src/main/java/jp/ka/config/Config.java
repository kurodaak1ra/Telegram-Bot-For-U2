package jp.ka.config;

import jp.ka.command.Command;
import jp.ka.command.impl.ToolsCommand;
import jp.ka.controller.CallbackResolver;
import jp.ka.controller.CommandResolver;
import jp.ka.utils.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class Config implements CommandLineRunner, ApplicationListener<ContextRefreshedEvent> {

  public static Long uid;
  @Value("${user.id}")
  public void setUID(Long uid) {
    this.uid = uid;
  }

  public static String cookieKey;
  @Value("${u2.cookie.key}")
  public void setCookieKey(String key) {
    this.cookieKey = key;
  }

  private static String cookieValue;
  @Value("${u2.cookie.value}")
  public void setCookieValue(String value) {
    this.cookieValue = value;
  }

  public static Command.CMD step;
  public static Map<String, String> session = new HashMap<>();

  @Override
  // 测试环境初始化参数
  public void run(String... args) throws Exception {
    if (!cookieValue.equals("")) {
      if (Objects.isNull(uid)) {
        log.info("手动设置了 Cookie 必须再手动设置 Telegram Number UID");
        System.exit(0);
      }
      Config.session.put(cookieKey, cookieValue);
      new ToolsCommand().setUserData(uid);
    }
    log.info("[Inject Cookie] {}", Config.session);
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext context = event.getApplicationContext();
    Store.context = context;
    context.getBean(CommandResolver.class).initCommandMap(context);
    context.getBean(CallbackResolver.class).initCallbackMap(context);
  }

}
