package jp.ka.callback;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Callback {

  void execute(Update update);
  CBK cbk();

  enum CBK {
    SIGN
  }

}
