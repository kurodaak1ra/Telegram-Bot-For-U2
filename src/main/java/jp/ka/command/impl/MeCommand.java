package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.command.CommandTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class MeCommand implements Command {

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    CommandTools.userInfo(gid);
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
    return "签名条";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
