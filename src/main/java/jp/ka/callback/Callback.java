package jp.ka.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

public interface Callback {

  void execute(CallbackQuery query, String cbData, Map<String, Object> cache);
  CBK cbk();

  enum CBK {
    CAPTCHA, SIGN, SEARCH, PUSH_SERVICE, TORRENT_INFO, TORRENT_LINK, TORRENT_MAGIC_FOR, TORRENT_MAGIC_HOURS, TORRENT_MAGIC_TYPE, MAGIC, U2_HIME
  }

}
