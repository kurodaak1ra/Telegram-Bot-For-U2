package jp.ka.push;

import jp.ka.bean.RespGet;
import jp.ka.config.Config;
import jp.ka.controller.Receiver;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.PhantomjsUtils;
import jp.ka.utils.Store;
import lombok.SneakyThrows;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.util.*;

public class PmPush {

  private static Timer timer = new Timer("pm_push");
  private static TimerTask timerTask = null;

  public static void start() {
    if (Objects.isNull(Config.id) || Objects.nonNull(timerTask) || Config.phantomjs.equals("")) return;
    Store.PM_PUSH = true;
    timerTask = new TimerTask() {
      @Override
      public void run() {
        pm();
      }
    };
    timer.purge();
    timer.schedule(timerTask, 0, 30 * 1000);
  }

  public static void stop() {
    if (Objects.isNull(timerTask)) return;
    Store.PM_PUSH = false;
    timerTask.cancel();
    timerTask = null;
  }

  private static void pm() {
    RespGet resp = HttpUtils.get(Config.id, "/messages.php");
    if (resp.getCode() != 200) return;
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
  }
  @SneakyThrows
  private static void pmNotice(String mid, String theme, String sid, String sname, String time) {
    String from = "`" + sname + "`";
    if (Objects.nonNull(sid)) from = String.format("[%s](%s/userdetails.php?id=%s)", CommonUtils.formatMD(sname), Config.U2Domain, sid);

    List<File> screens = PhantomjsUtils.captureEl(Config.U2Domain + "/messages.php?action=viewmessage&id=" + mid, "#outer table:last-child");
    for (File screen : screens) {
      Store.context.getBean(Receiver.class).sendImg( Config.id, "md", String.format("*PM消息提醒*\n\n主题: [%s](%s/messages.php?action=viewmessage&id=%s)\n来自: %s\n时间: `%s`",
        CommonUtils.formatMD(theme), Config.U2Domain, mid, from, CommonUtils.formatMD(time)), new InputFile().setMedia(screen, "pm.png"), null);
    }
  }

}
