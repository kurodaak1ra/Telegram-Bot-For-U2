package jp.ka.command;

import jp.ka.controller.Receiver;
import jp.ka.controller.Resolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@Component
public class AllCommand extends Command {

  @Autowired
  private Receiver receiver;

  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    StringBuilder builder = new StringBuilder();
    CMD[] cmds = CMD.values();
    for (CMD cmd : cmds) {
      Command command = applicationContext.getBean(Resolver.class).getCommandMap().get(cmd.name());
      if (Objects.isNull(command)) continue;
      builder.append("/").append(cmd.name().toLowerCase()).append(" - ").append(command.description()).append("\n");
    }
    receiver.sendMsg(gid, builder.toString(), "", -1);
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
