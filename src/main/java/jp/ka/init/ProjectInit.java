package jp.ka.init;

import jp.ka.bean.*;
import jp.ka.bean.config.U2;
import jp.ka.bean.config.User;
import jp.ka.command.CommandTools;
import jp.ka.controller.CallbackResolver;
import jp.ka.controller.CommandResolver;
import jp.ka.mapper.U2Mapper;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.PhantomjsUtils;
import jp.ka.variable.Store;
import jp.ka.variable.U2Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class ProjectInit implements CommandLineRunner, ApplicationListener<ContextRefreshedEvent> {

  @Autowired
  private U2Mapper mapper;

  @Autowired
  private U2 u2;

  @Autowired
  private User user;

  @Override // 初始化数据
  public void run(String... args) throws Exception {
    UserInfo info = mapper.queryInfo();
    UserCookie[] cookies = mapper.queryCookies();
    if (Objects.nonNull(info) && Objects.nonNull(cookies)) {
      user.setUid(info.getId());
      U2Info.uid = user.getUid().toString();
      U2Info.pageKey = info.getPageKey();
      U2Info.passKey = info.getPassKey();
      for (UserCookie cookie : cookies) {
        HttpUtils.session.put(cookie.getK(), cookie.getV());
      }
      PhantomjsUtils.init();
      CommandTools.userInfo(user.getUid());
      CommonUtils.pushServiceStart();
      log.info("[Login Data From SQL]");
      return;
    }

    if (!u2.getCookie().equals("")) {
      if (Objects.isNull(user.getUid())) {
        log.info("手动设置了 Cookie 必须再手动设置 Telegram Number UID");
        System.exit(0);
      }
      for (String cookie : u2.getCookie().split(";")) {
        String[] split = cookie.split("=");
        if (split.length == 2) {
          HttpUtils.session.put(split[0], split[1]);
          mapper.insertCookies(new UserCookie(split[0], split[1]));
        }
      }
      CommandTools.loginSucc(user.getUid());
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
