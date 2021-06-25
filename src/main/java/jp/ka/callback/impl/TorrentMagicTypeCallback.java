package jp.ka.callback.impl;

import jp.ka.bean.RespPost;
import jp.ka.callback.Callback;
import jp.ka.command.impl.MagicController;
import jp.ka.controller.Receiver;
import jp.ka.variable.Store;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;
import java.util.Map;

@Component
public class TorrentMagicTypeCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    switch (cbData) {
      case "pre": {
        Store.context.getBean(TorrentMagicForCallback.class).sendBtn(gid, mid);
        break;
      }
      case "close": {
        Store.TORRENT_INFO_MESSAGE_ID = -1;
        break;
      }
      default: {
        Store.TORRENT_INFO_MESSAGE_ID = -1;
        Store.TORRENT_MAGIC_TYPE = cbData;
        sendMagic(gid, mid);
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_MAGIC_TYPE;
  }

  @SneakyThrows
  private void sendMagic(Long gid, Integer mid) {
    receiver.sendDel(gid, mid);
    if (Store.TORRENT_MAGIC_TID.equals("") || Store.TORRENT_MAGIC_FOR.equals("") || Store.TORRENT_MAGIC_HOURS.equals("") || Store.TORRENT_MAGIC_TYPE.equals("")) {
      receiver.sendMsg(gid, "md", "*数据异常，请重试*", null);
      return;
    }

    Map<String, Object> map = Store.context.getBean(MagicController.class).magicPre(gid, Store.TORRENT_MAGIC_TID, Store.TORRENT_MAGIC_FOR, Store.TORRENT_MAGIC_HOURS, Store.TORRENT_MAGIC_TYPE, "1.00", "1.00");
    List<NameValuePair> params = (List<NameValuePair>) map.get("params");

    RespPost resp = Store.context.getBean(MagicCallback.class).magic(gid, Store.TORRENT_MAGIC_TID, params);
    if (resp.getCode() == 200) {
      Map<String, String> fee = (Map<String, String>) map.get("fee");
      receiver.sendMsg(gid, "md", String.format("*魔法施放成功*\n\n费用: %s%s%s",
        fee.get("gold").equals("") ? "" : "\uD83E\uDD47" + fee.get("gold"),
        fee.get("silver").equals("") ? "" : "\uD83E\uDD48" + fee.get("silver"),
        fee.get("copper").equals("") ? "" : "\uD83E\uDD49" + fee.get("copper")
      ), null);
    }
  }

}
