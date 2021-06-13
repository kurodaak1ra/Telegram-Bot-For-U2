package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
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

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, Map<String, Object> cache) {
    receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_WAITING);
    redis.del(Store.TORRENT_LINK_MESSAGE_ID_KEY);
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_LINK;
  }

}
