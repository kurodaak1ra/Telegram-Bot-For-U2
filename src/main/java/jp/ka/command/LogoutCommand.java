package jp.ka.command;

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
public class LogoutCommand extends Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    receiver.sendMsg(gid, Text.WAITING, "md", -1);
    try {
      Config.session.clear();
      HttpUtils.get(gid, "/logout.php?key=" + U2.pageKey);
      receiver.sendMsg(gid, "*登出成功！*", "md", -1);
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
