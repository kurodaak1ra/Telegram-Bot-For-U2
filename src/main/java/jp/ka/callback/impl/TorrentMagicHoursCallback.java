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
public class TorrentMagicHoursCallback implements Callback {

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
        sendBtn(gid, mid, tid, forr, (Integer) cache.get("hours"));
        break;
      }
      case "pre": {
        Store.context.getBean(TorrentInfoCallback.class).magic(gid, mid, tid);
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
    return CBK.TORRENT_MAGIC_HOURS;
  }

  private String cacheData(String source, String tid, String forr, Integer hours, Integer type) {
    String uuid = UUID.randomUUID().toString();

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", source);
    if (Objects.nonNull(tid)) map.put("tid", tid);
    if (Objects.nonNull(forr)) map.put("for", forr);
    if (Objects.nonNull(tid)) map.put("hours", hours);
    if (Objects.nonNull(tid)) map.put("type", type);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

  public void sendBtn(Long gid, Integer mid, String tid, String forr, Integer hours) {
    receiver.sendEditMsg(gid, mid, "md", "*请选择优惠效果*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("Free", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("data", tid, forr, hours, 2)),
        Arrays.asList("30%", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("data", tid, forr, hours, 7)),
        Arrays.asList("50%", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("data", tid, forr, hours, 5))
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("2X", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("data", tid, forr, hours, 3)),
        Arrays.asList("2X 50%", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("data", tid, forr, hours, 6)),
        Arrays.asList("2X Free", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("data", tid, forr, hours, 4))
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("⬅️ 上一步", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("pre", null, null, null, null)),
        Arrays.asList("❌", CBK.TORRENT_MAGIC_TYPE + ":" + cacheData("close", null, null, null, null))
      ))
    ));
  }

}
