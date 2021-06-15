package jp.ka.callback.impl;

import jp.ka.bean.RespPost;
import jp.ka.callback.Callback;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;
import java.util.Map;

@Component
public class MagicCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    String source = (String) cache.get("source");
    switch (source) {
      case "params": {
        List<NameValuePair> params = (List<NameValuePair>) cache.get("params");

        String tid = "";
        for (NameValuePair param : params) {
          if (param.getName().equals("torrent")) tid = param.getValue();
        }

        receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_WAITING);
        try {
          RespPost resp = magic(gid, tid, params);
          if (resp.getCode() == 200) {
            receiver.sendDel(gid, mid);
            receiver.sendMsg(gid, "md", "*魔法施放成功*", null);
          }
        } catch (HttpException e) { }
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.MAGIC;
  }

  public RespPost magic(Long gid, String tid, List<NameValuePair> params) throws HttpException {
    return HttpUtils.postForm(gid, "/promotion.php?action=magic&torrent=" + tid, params);
  }

}
