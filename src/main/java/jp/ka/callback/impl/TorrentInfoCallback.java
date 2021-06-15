package jp.ka.callback.impl;

import jp.ka.callback.Callback;
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

import java.util.*;

@Component
public class TorrentInfoCallback implements Callback {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_WAITING);
    redis.expired(Store.TORRENT_INFO_MESSAGE_ID_KEY, Store.TTL);
    String tid = (String) cache.get("tid");
    String source = (String) cache.get("source");
    switch (source) {
      case "torrent_link": {
        link(gid, mid, tid);
        break;
      }
      case "magic": {
        magic(gid, mid, tid);
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
    return CBK.TORRENT_INFO;
  }

  private void link(Long gid, Integer mid, String tid) {
    String uuid = UUID.randomUUID().toString();
    Map<String, Object> map = new HashMap<>();
    map.put("source", "close");
    redis.set(uuid, map, Store.TTL);

    String text = String.format("链接包含私密的 passkey 请谨慎使用\n\n%s/download.php?id=%s&passkey=%s&https=1", Config.U2Domain, tid, U2.passKey);
    receiver.sendEditMsg(gid, mid, "md", CommonUtils.formatMD(text), Arrays.asList(Arrays.asList(Arrays.asList(
        Arrays.asList("❌", CBK.TORRENT_LINK + ":" + uuid)
    ))));
  }

  public void magic(Long gid, Integer mid, String tid) {
    receiver.sendEditMsg(gid, mid, "md", "*请选择施放对象*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("全体", CBK.TORRENT_MAGIC_FOR + ":" + cacheData("data", tid, "ALL")),
        Arrays.asList("自己", CBK.TORRENT_MAGIC_FOR + ":" + cacheData("data", tid, U2.uid))
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("❌", CBK.TORRENT_MAGIC_FOR + ":" + cacheData("close", null, null))
      ))
    ));
  }

  private String cacheData(String source, String tid, String forr) {
    String uuid = UUID.randomUUID().toString();

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", source);
    if (Objects.nonNull(forr)) map.put("for", forr);
    if (Objects.nonNull(tid)) map.put("tid", tid);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }
}
