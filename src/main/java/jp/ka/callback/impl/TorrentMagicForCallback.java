package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.controller.Receiver;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.*;

@Component
public class TorrentMagicForCallback implements Callback {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    String source = (String) cache.get("source");
    switch (source) {
      case "data": {
        sendBtn(gid, mid, (String) cache.get("tid"), (String) cache.get("for"));
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
    return CBK.TORRENT_MAGIC_FOR;
  }

  private String cacheData(String source, String tid, String forr, Integer hours) {
    String uuid = UUID.randomUUID().toString();

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", source);
    if (Objects.nonNull(tid)) map.put("tid", tid);
    if (Objects.nonNull(forr)) map.put("for", forr);
    if (Objects.nonNull(tid)) map.put("hours", hours);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

  public void sendBtn(Long gid, Integer mid, String tid, String forr) {
    receiver.sendEditMsg(gid, mid, "md", "*请选择有效时限*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("24H", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("data", tid, forr, 24)),
        Arrays.asList("36H", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("data", tid, forr, 36)),
        Arrays.asList("48H", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("data", tid, forr, 48)),
        Arrays.asList("96H", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("data", tid, forr, 96)),
        Arrays.asList("120H", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("data", tid, forr, 120)),
        Arrays.asList("192H", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("data", tid, forr, 192)),
        Arrays.asList("360H", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("data", tid, forr, 360))
      )),
      Arrays.asList(Arrays.asList(
          Arrays.asList("⬅️ 上一步", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("pre", null, null, null)),
          Arrays.asList("❌", CBK.TORRENT_MAGIC_HOURS + ":" + cacheData("close", null, null, null))
      ))
    ));
  }

}
