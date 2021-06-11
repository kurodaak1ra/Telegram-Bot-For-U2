package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.controller.CommandResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Component
public class AllCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    StringBuilder builder = new StringBuilder();
    for (CMD cmd : CMD.values()) {
      Command command = applicationContext.getBean(CommandResolver.class).getCommandMap().get(cmd.name());
      if (Objects.isNull(command)) continue;
      builder.append("/").append(cmd.name().toLowerCase()).append(" - ").append(command.description()).append("\n");
    }
    receiver.sendMsg(gid, "", builder.toString(), null);
  }

  @Override
  public CMD cmd() {
    return CMD.ALL;
  }

  @Override
  public Boolean needLogin() {
    return false;
  }

  @Override
  public String description() {
    return "全部指令";
  }

}
