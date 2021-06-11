package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.callback.CallbackTools;
import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
public class TorrentInfoCallback implements Callback {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    CallbackQuery query = update.getCallbackQuery();
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    Map<String, Object> cache = CallbackTools.hasExpired(gid, query);
    if (Objects.isNull(cache)) return;

    Integer torrentLinkMsgID = (Integer) redis.get(Store.TORRENT_LINK_MESSAGE_ID_KEY);

    receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_WAITING);
    String source = (String) cache.get("source");
    switch (source) {
      case "torrent_link": {
        String tid = (String) cache.get("tid");
        sendTorrentLink(gid, tid, torrentLinkMsgID);
        break;
      }
      case "close": {
        if (Objects.nonNull(torrentLinkMsgID)) {
          receiver.sendDel(gid, torrentLinkMsgID);
          redis.del(Store.TORRENT_INFO_MESSAGE_ID_KEY);
        }
        receiver.sendDel(gid, mid);
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_INFO;
  }

  private void sendTorrentLink(Long gid, String tid, Integer torrentLinkMsgID) {
    List<List<List<List<String>>>> columns = Arrays.asList(Arrays.asList(Arrays.asList(Arrays.asList("❌", CBK.TORRENT_LINK + ":close"))));
    String text = String.format("链接包含私密的 passkey 请谨慎使用\n\n%s/download.php?id=%s&passkey=%s&https=1", Config.U2Domain, tid, U2.passKey);

    if (Objects.isNull(torrentLinkMsgID)) {
      Message msg = receiver.sendMsg(gid, "md", CommonUtils.formatMD(text), columns);
      redis.set(Store.TORRENT_LINK_MESSAGE_ID_KEY, msg.getMessageId(), Store.TTL);
    } else {
      receiver.sendEditMsg(gid, torrentLinkMsgID, "md", CommonUtils.formatMD(text), columns);
      redis.expired(Store.TORRENT_LINK_MESSAGE_ID_KEY, Store.TTL);
    }
  }

}
