package jp.ka.crontab;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.util.List;

@Component
public class Crontab {

  @SneakyThrows
  @Scheduled(cron="0/30 * * * * ?")
  public void pm() {
    if (Config.phantomjs.equals("")) return;

    RespGet resp = HttpUtils.get(Config.uid, "/messages.php");
    if (resp.getCode() != 200) return;
    Elements tables = resp.getHtml().getElementById("outer").getElementsByTag("table");
    Element content = tables.get(tables.size() - 1);

    Elements tr = content.getElementsByTag("tr");
    for (int i = 1; i < tr.size() - 2; i++) {
      String flag = tr.get(i).getElementsByTag("img").get(0).attr("class");
      if (!flag.equals("unreadpm")) continue;
      String pmId = tr.get(i).getElementsByTag("input").get(0).attr("value");
      String pmTitle = tr.get(i).getElementsByTag("td").get(1).text();
      String pmTime = tr.get(i).getElementsByTag("time").get(0).attr("title");
      Element pmUser = tr.get(i).getElementsByTag("td").get(2);
      String pmUid = pmUser.getElementsByTag("a").get(0).attr("href").split("=")[1];
      String pmUsername = pmUser.getElementsByTag("bdo").get(0).text();

      sendNotice(pmId, pmTitle, pmTime, pmUid, pmUsername);
    }
  }

  @SneakyThrows
  private static void sendNotice(String id, String title, String time, String uid, String username) {
    List<File> screens = PhantomjsUtils.captureEl(Config.U2Domain + "/messages.php?action=viewmessage&id=" + id, "#outer>table:last-child");
    for (File screen : screens) {
      Store.context.getBean(Receiver.class).sendImg(
        Config.uid,
        "md",
        String.format("*PM消息*\n\n发讯者: [%s](%s/userdetails.php?id=%s)\n时间: `%s`\n主题: [%s](%s/messages.php?action=viewmessage&id=%s)",
            username, Config.U2Domain, uid, CommonUtils.formatMD(time), title, Config.U2Domain, id),
        new InputFile().setMedia(screen),
        null
      );
    }
  }

}
