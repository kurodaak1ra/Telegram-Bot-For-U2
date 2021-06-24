package jp.ka.callback.impl;

import jp.ka.bean.RespPost;
import jp.ka.callback.Callback;
import jp.ka.config.BotInitializer;
import jp.ka.variable.U2;
import jp.ka.controller.Receiver;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class U2HimeCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @SneakyThrows
  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("shbox_text", "U2娘 " + cbData));
    params.add(new BasicNameValuePair("shout", "这不是搜索！"));

    receiver.sendDel(gid, mid);
    RespPost resp = HttpUtils.postForm(gid, "/shoutbox.php?action=send&key=" + U2.pageKey, params);
    if (resp.getCode() == 200) {
      Element shoutrow = resp.getHtml().getElementsByClass("shoutrow").get(0);
      Elements reply = shoutrow.getElementsByTag("div").get(0).getElementsByTag("bdo").get(0).getAllElements();

      String replyMsg = reply.get(0).text();
      if (reply.size() == 2) {
        String imgSrc = reply.get(1).attr("src");
        if (!imgSrc.equals("")) {
            String      uri = imgSrc.replaceAll(BotInitializer.U2Domain, "");
            InputStream pic = HttpUtils.getPic(gid, uri.charAt(0) == '/' ? uri : "/" + uri);
          if (!replyMsg.equals("")) replyMsg = "*U2娘* " + CommonUtils.formatMD(replyMsg);
          receiver.sendImg(gid, "md", replyMsg, new InputFile().setMedia(pic, "img.png"), null);
          return;
        }
      }
      receiver.sendMsg(gid, "md", "*U2娘* " + CommonUtils.formatMD(replyMsg), null);
    }
  }

  @Override
  public CBK cbk() {
    return CBK.U2_HIME;
  }

}
