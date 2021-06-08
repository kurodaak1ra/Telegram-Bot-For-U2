package jp.ka.controller;

import jp.ka.command.Command;
import jp.ka.config.Config;
import jp.ka.utils.Store;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CommandResolver {

  private final Map<String, Command> commandMap = new HashMap<>();

  public void initCommandMap(ApplicationContext context) {
    context.getBeansOfType(Command.class).values().forEach(this::putCommands);
  }

  private void putCommands(Command command) {
    commandMap.put(command.cmd().name(), command);
  }

  public Map<String, Command> getCommandMap() {
    return commandMap;
  }

  public void executeCommand(Update update) {
    String commandText = update.getMessage().getText().toUpperCase().replaceAll("\n", " ").split(" ")[0];
    if (commandText.charAt(0) == '/') {
      Command command = commandMap.get(commandText.substring(1));
      if (Objects.isNull(command)) return;

      boolean hasCookie = Config.session.containsKey(Config.cookieKey);
      if ((hasCookie && (!command.getClass().getSimpleName().equals("CaptchaCommand") && !command.getClass().getSimpleName().equals("LoginCommand"))) || (!hasCookie && !command.needLogin())) {
        command.execute(update);
        return;
      }

      String errMsg = "*请登陆*";
      if (hasCookie) errMsg = "*您已登陆*";
      Store.context.getBean(Receiver.class).sendMsg(update.getMessage().getChatId(), errMsg, "md");
    }
  }

}
