package jp.ka.callback.impl;

import jp.ka.bean.RespPost;
import jp.ka.callback.Callback;
import jp.ka.command.impl.MagicController;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TorrentMagicTypeCallback implements Callback {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    String source = (String) cache.get("source");
    String tid = (String) cache.get("tid");
    String forr = (String) cache.get("for");
    switch (source) {
      case "data": {
        receiver.sendDel(gid, mid);
        receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_WAITING);
        redis.del(Store.TORRENT_INFO_MESSAGE_ID_KEY);
        data(gid, tid, forr, (Integer) cache.get("hours"), (Integer) cache.get("type"));
        break;
      }
      case "pre": {
        Store.context.getBean(TorrentMagicForCallback.class).sendBtn(gid, mid, tid, forr);
        break;
      }
      case "close": {
        redis.del(Store.TORRENT_INFO_MESSAGE_ID_KEY);
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_MAGIC_TYPE;
  }

  private void data(Long gid, String tid, String forr, Integer hours, Integer type) {
    Map<String, Object> map = Store.context.getBean(MagicController.class).magicPre(gid, tid, forr, hours.toString(), type.toString(), "1.00", "1.00");
    List<NameValuePair> params = (List<NameValuePair>) map.get("params");

    try {
      RespPost resp = Store.context.getBean(MagicCallback.class).magic(gid, tid, params);
      if (resp.getCode() == 200) {
        Map<String, String> fee = (Map<String, String>) map.get("fee");
        receiver.sendMsg(gid, "md", String.format("*魔法释放成功*\n\n费用: %s%s%s",
          fee.get("gold").equals("") ? "" : "\uD83E\uDD47" + fee.get("gold"),
          fee.get("silver").equals("") ? "" : "\uD83E\uDD48" + fee.get("silver"),
          fee.get("copper").equals("") ? "" : "\uD83E\uDD49" + fee.get("copper")
        ), null);
      }
    } catch (HttpException e) { }
  }

}
