package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.utils.Store;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

@Component
public class TorrentLinkCallback implements Callback {

  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    switch (cbData) {
      case "close": {
        Store.TORRENT_INFO_MESSAGE_ID = -1;
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.TORRENT_LINK;
  }

}
