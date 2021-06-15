package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

@Component
public class TorrentLinkCallback implements Callback {

  @Autowired
  private RedisUtils redis;

  @Override
  public void execute(CallbackQuery query, Map<String, Object> cache) {
    String source = (String) cache.get("source");
    switch (source) {
      case "close": {
        redis.del(Store.TORRENT_INFO_MESSAGE_ID_KEY);
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_LINK;
  }

}
