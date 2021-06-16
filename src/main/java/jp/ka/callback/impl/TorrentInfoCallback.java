package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.config.Config;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.*;

@Component
public class TorrentInfoCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    String tid = (String) cache.get("tid");
    String source = (String) cache.get("source");
    switch (source) {
      case "torrent_link": {
        link(gid, mid, tid);
        break;
      }
      case "magic": {
        Store.TORRENT_MAGIC_TID = tid;
        Store.TORRENT_MAGIC_FOR = "";
        Store.TORRENT_MAGIC_HOURS = "";
        Store.TORRENT_MAGIC_TYPE = "";
        magic(gid, mid);
        break;
      }
      case "close": {
        Store.TORRENT_INFO_MESSAGE_ID = -1;
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_INFO;
  }

  private void link(Long gid, Integer mid, String tid) {
    String text = String.format("链接包含私密的 passkey 请谨慎使用\n\n%s/download.php?id=%s&passkey=%s&https=1", Config.U2Domain, tid, U2.passKey);
    receiver.sendEditMsg(gid, mid, "md", CommonUtils.formatMD(text), Arrays.asList(Arrays.asList(Arrays.asList(
      Arrays.asList("❌", CBK.TORRENT_LINK + ":close")
    ))));
  }

  public void magic(Long gid, Integer mid) {
    receiver.sendEditMsg(gid, mid, "md", "*请选择施放对象*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("全体", CBK.TORRENT_MAGIC_FOR + ":ALL"),
        Arrays.asList("自己", CBK.TORRENT_MAGIC_FOR + ":" + U2.uid)
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("❌", CBK.TORRENT_MAGIC_FOR + ":close")
      ))
    ));
  }

}
