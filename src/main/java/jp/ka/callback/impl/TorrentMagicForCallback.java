package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.controller.Receiver;
import jp.ka.variable.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.*;

@Component
public class TorrentMagicForCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    switch (cbData) {
      case "close": {
        Store.TORRENT_INFO_MESSAGE_ID = -1;
        break;
      }
      default: {
        Store.TORRENT_MAGIC_FOR = cbData;
        sendBtn(gid, mid);
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_MAGIC_FOR;
  }

  public void sendBtn(Long gid, Integer mid) {
    receiver.sendEditMsg(gid, mid, "md", "*请选择有效时限*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("24H", CBK.TORRENT_MAGIC_HOURS + ":24"),
        Arrays.asList("36H", CBK.TORRENT_MAGIC_HOURS + ":36"),
        Arrays.asList("48H", CBK.TORRENT_MAGIC_HOURS + ":48"),
        Arrays.asList("96H", CBK.TORRENT_MAGIC_HOURS + ":96"),
        Arrays.asList("120H", CBK.TORRENT_MAGIC_HOURS + ":120"),
        Arrays.asList("192H", CBK.TORRENT_MAGIC_HOURS + ":192"),
        Arrays.asList("360H", CBK.TORRENT_MAGIC_HOURS + ":360")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("⬅️ 上一步", CBK.TORRENT_MAGIC_HOURS + ":pre"),
        Arrays.asList("❌", CBK.TORRENT_MAGIC_HOURS + ":close")
      ))
    ));
  }

}
