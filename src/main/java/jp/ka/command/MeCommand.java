package jp.ka.command;

import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class MeCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Message msg = update.getMessage();
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, Text.WAITING, "md");
    ToolsCommand.setUserData(gid);
  }

  @Override
  public CMD cmd() {
    return CMD.ME;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "个人信息";
  }

}
