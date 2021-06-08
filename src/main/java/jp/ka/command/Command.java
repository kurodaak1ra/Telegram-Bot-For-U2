package jp.ka.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {

  void execute(Update update);
  CMD cmd();
  Boolean needLogin();
  String description();

  enum CMD {
    ALL, CAPTCHA, LOGIN, LOGOUT, ME, TRANSFER, TRANSFER_INFO, TRANSFER_DELETE, TRANSFER_CANCEL, SIGN
  }

}
