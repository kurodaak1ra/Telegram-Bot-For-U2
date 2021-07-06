package jp.ka.push;

import jp.ka.bean.config.Phantomjs;
import jp.ka.bean.RespGet;
import jp.ka.bean.config.U2;
import jp.ka.bean.config.User;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.PhantomjsUtils;
import jp.ka.variable.MsgTpl;
import jp.ka.variable.Store;
import lombok.SneakyThrows;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class PmPush {

  private static U2 u2;
  private static User user;
  private static Phantomjs phantomjs;

  @Autowired
  public void setU2(U2 u2) {
    this.u2 = u2;
  }
  @Autowired
  public void setUser(User user) {
    this.user = user;
  }
  @Autowired
  public void setPhantomjs(Phantomjs phantomjs) {
    this.phantomjs = phantomjs;
  }

  private static Timer timer;
  private static TimerTask timerTask;

  public static void start() {
    if (Objects.isNull(user.getId()) || Objects.nonNull(timerTask) || phantomjs.getPath().equals("")) return;
    Store.PM_PUSH = true;
    timerTask = new TimerTask() {
      @Override
      public void run() {
        pm();
      }
    };

    timer = new Timer("pm_push");
    // timer.purge();
    timer.schedule(timerTask, 0, Store.PUSH_INTERVAL_TIME);
  }

  public static void stop() {
    if (Objects.isNull(timerTask)) return;
    Store.PM_PUSH = false;
    timerTask.cancel();
    timerTask = null;
    timer = null;
  }

  private static void pm() {
    RespGet resp = null;
    try {
      resp = HttpUtils.get(user.getId(), "/messages.php");
      Elements tables = resp.getHtml().getElementById("outer").getElementsByTag("table");
      Element content = tables.get(tables.size() - 1);

      Elements rows = content.child(0).children();
      for (int i = 1; i < rows.size() - 2; i++) {
        Element columns = rows.get(i);
        if (!columns.child(0).child(0).attr("class").equals("unreadpm")) continue;
        Element msg = columns.child(1).child(0);
        String mid = msg.attr("href").split("id=")[1];
        String theme = msg.text();

        Elements tmpSender = columns.child(2).getElementsByTag("a");
        Element sender = columns.child(2);
        if (tmpSender.size() != 0) sender = tmpSender.get(0);
        String[] sid = sender.attr("href").split("id=");
        String sname = sender.text();

        String time =  columns.child(3).child(0).attr("title");

        pmNotice(mid, theme, sid.length != 2 ? null : sid[1], sname, time);
      }
      Store.PM_PUSH_REQ_FAILED_TIMES = 0;
    } catch (HttpException e) {
      if (resp.getCode() >= 500) {
        if (Store.PM_PUSH_REQ_FAILED_TIMES >= 10) {
          stop();
          Store.PM_PUSH_REQ_FAILED_TIMES = 0;
          Store.context.getBean(Receiver.class).sendMsg(user.getId(), "md", String.format(MsgTpl.PUSH_FAILED_MULTIPLE_TIMES, "PM"), null);
        } else Store.PM_PUSH_REQ_FAILED_TIMES++;
      }
    }
  }

  @SneakyThrows
  private static void pmNotice(String mid, String theme, String sid, String sname, String time) {
    String from = "`" + sname + "`";
    if (Objects.nonNull(sid)) from = String.format("[%s](%s/userdetails.php?id=%s)", CommonUtils.formatMD(sname), u2.getDomain(), sid);

    List<File> screens = PhantomjsUtils.captureEl(u2.getDomain() + "/messages.php?action=viewmessage&id=" + mid, "#outer table:last-child");
    for (File screen : screens) {
      Store.context.getBean(Receiver.class).sendImg(user.getId(), "md", String.format("*PM消息提醒*\n\n主题: [%s](%s/messages.php?action=viewmessage&id=%s)\n来自: %s\n时间: `%s`",
        CommonUtils.formatMD(theme), u2.getDomain(), mid, from, CommonUtils.formatMD(time)), new InputFile().setMedia(screen, "pm.png"), null);
    }
  }

}
