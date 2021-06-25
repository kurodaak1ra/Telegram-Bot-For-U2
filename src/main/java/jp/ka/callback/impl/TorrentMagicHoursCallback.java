package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.controller.Receiver;
import jp.ka.variable.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.*;

@Component
public class TorrentMagicHoursCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    switch (cbData) {
      case "pre": {
        Store.context.getBean(TorrentInfoCallback.class).magic(gid, mid);
        break;
      }
      case "close": {
        Store.TORRENT_INFO_MESSAGE_ID = -1;
        break;
      }
      default: {
        Store.TORRENT_MAGIC_HOURS = cbData;
        sendBtn(gid, mid);
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_MAGIC_HOURS;
  }

  public void sendBtn(Long gid, Integer mid) {
    receiver.sendEditMsg(gid, mid, "md", "*请选择优惠效果*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("Free", CBK.TORRENT_MAGIC_TYPE + ":2"),
        Arrays.asList("30%", CBK.TORRENT_MAGIC_TYPE + ":7"),
        Arrays.asList("50%", CBK.TORRENT_MAGIC_TYPE + ":5")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("2X", CBK.TORRENT_MAGIC_TYPE + ":3"),
        Arrays.asList("2X 50%", CBK.TORRENT_MAGIC_TYPE + ":6"),
        Arrays.asList("2X Free", CBK.TORRENT_MAGIC_TYPE + ":4")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("⬅️ 上一步", CBK.TORRENT_MAGIC_TYPE + ":pre"),
        Arrays.asList("❌", CBK.TORRENT_MAGIC_TYPE + ":close")
      ))
    ));
  }

}
