package jp.ka.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class Command {

  public abstract void execute(Update update);
  public abstract CMD cmd();
  public abstract Boolean needLogin();
  public abstract String description();

  public enum CMD {
    CAPTCHA, LOGIN, LOGOUT, ME, TRANSFER, TRANSFER_INFO, TRANSFER_CANCEL
  }

}
