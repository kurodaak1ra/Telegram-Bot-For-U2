package jp.ka.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

public interface Callback {

  void execute(CallbackQuery query, Map<String, Object> cache);
  CBK cbk();

  enum CBK {
    SIGN, SEARCH, TORRENT_INFO, TORRENT_LINK, MAGIC
  }

}
