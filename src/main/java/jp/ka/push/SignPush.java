package jp.ka.push;

import jp.ka.bean.RespGet;
import jp.ka.bean.config.User;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.variable.Store;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Kurenai
 * @since 2021-09-26
 */
@Component
public class SignPush {

  private static User       user;

  @Autowired
  public void setUser(User user) {
    SignPush.user = user;
  }

  private static Timer     timer     = new Timer("sign_push");
  private static TimerTask timerTask = null;

  public static void start() {
    timerTask = new TimerTask() {

      /**
       * The action to be performed by this timer task.
       */
      @Override
      public void run() {
        if (Objects.isNull(  user.getId()) || Objects.nonNull(timerTask)) return;
        Receiver receiver = Store.context.getBean(Receiver.class);
        Long gid = user.getId();

        try {
          RespGet  resp     = HttpUtils.get(gid, "/showup.php");
          Elements elements = resp.getHtml().getElementsByClass("captcha");
          if (elements.size() == 0) {
            return;
          }
          receiver.sendMsg(gid, "md", "您今天还未进行签到。", null);

        } catch (HttpException ignored) {
        }
      }
    };

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime first  = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 12, 0);
    if (now.isAfter(first)) {
      first = first.plusDays(1);
    }
    timer.purge();
    timer.schedule(timerTask, Timestamp.valueOf(first), TimeUnit.DAYS.toMillis(1));
  }

  public static void stop() {
    if (Objects.isNull(timerTask)) return;
    Store.SIGN_PUSH = false;
    timerTask.cancel();
    timerTask = null;
  }

}
