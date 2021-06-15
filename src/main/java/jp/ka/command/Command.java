package jp.ka.command;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface Command {

  void execute(Message msg);
  CMD cmd();
  Boolean needLogin();
  String description();
  Message prompt(Long gid);

  enum CMD {
    ALL, CAPTCHA, LOGIN, LOGOUT, ME, MY_BAR, TRANSFER, TRANSFER_INFO, TRANSFER_DELETE, TRANSFER_CANCEL, SIGN, SEARCH, MAGIC, U2_HIME
  }

}
