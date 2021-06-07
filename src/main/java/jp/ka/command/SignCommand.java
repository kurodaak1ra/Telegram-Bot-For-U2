package jp.ka.command;

import jp.ka.controller.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SignCommand extends Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {

  }

  @Override
  public CMD cmd() {
    return CMD.SIGN;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "签到";
  }

}
