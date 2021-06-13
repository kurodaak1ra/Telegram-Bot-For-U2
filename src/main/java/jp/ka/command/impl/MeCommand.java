package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.command.CommandTools;
import jp.ka.config.Text;
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
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, "md", Text.WAITING, null);
    CommandTools.setUserData(gid);
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

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
