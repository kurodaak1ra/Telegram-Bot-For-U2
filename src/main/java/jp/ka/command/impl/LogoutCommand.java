package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class LogoutCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    receiver.sendMsg(gid, Text.WAITING, "md");
    try {
      Config.session.clear();
      HttpUtils.get(gid, "/logout.php?key=" + U2.pageKey);
      receiver.sendMsg(gid, "*登出成功！*", "md");
    } catch (HttpException e) { }
  }

  @Override
  public CMD cmd() {
    return CMD.LOGOUT;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "登出";
  }

}
