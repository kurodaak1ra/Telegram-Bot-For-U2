package jp.ka.callback.impl;

import jp.ka.bean.config.U2;
import jp.ka.callback.Callback;
import jp.ka.variable.U2Info;
import jp.ka.controller.Receiver;
import jp.ka.utils.CommonUtils;
import jp.ka.variable.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Arrays;
import java.util.Map;

@Component
public class TorrentInfoCallback implements Callback {

  @Autowired
  private U2 u2;

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
    receiver.sendEditMsg(gid, mid, "md",
      "*链接包含私密的 passkey 请谨慎使用*\n\n" + CommonUtils.formatMD(String.format("%s/download.php?id=%s&passkey=%s&https=1", u2.getDomain(), tid, U2Info.passKey)),
      Arrays.asList(Arrays.asList(Arrays.asList(
        Arrays.asList("❌", CBK.TORRENT_LINK + ":close")
      )))
    );
  }

  public void magic(Long gid, Integer mid) {
    receiver.sendEditMsg(gid, mid, "md", "*请选择施放对象*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("全体", CBK.TORRENT_MAGIC_FOR + ":ALL"),
        Arrays.asList("自己", CBK.TORRENT_MAGIC_FOR + ":" + U2Info.uid)
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("❌", CBK.TORRENT_MAGIC_FOR + ":close")
      ))
    ));
  }

}
