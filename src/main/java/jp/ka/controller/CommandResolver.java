package jp.ka.controller;

import jp.ka.command.Command;
import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.utils.Store;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

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
    Message msg = update.getMessage();

    String commandText = msg.getText().toUpperCase().replaceAll("\n", " ").split(" ")[0];
    if (commandText.charAt(0) == '/') {
      Command command = commandMap.get(commandText.substring(1));
      if (Objects.isNull(command)) return;

      boolean hasCookie = Config.session.containsKey(Config.cookieKey);
      if ((hasCookie && (!command.getClass().getSimpleName().equals("CaptchaCommand") && !command.getClass().getSimpleName().equals("LoginCommand"))) || (!hasCookie && !command.needLogin())) {
        String firstLine = msg.getText().toUpperCase().split("\n")[0].trim();
        if (firstLine.contains(" ")) {
          Message prompt = command.prompt(msg.getChatId());
          if (Objects.nonNull(prompt)) return;
        }
        Message waiting = Store.context.getBean(Receiver.class).sendMsg(msg.getChatId(), "md", Text.WAITING, null);
        new Timer().schedule(new TimerTask() {
          @Override
          public void run() {
          Store.context.getBean(Receiver.class).sendDel(msg.getChatId(), waiting.getMessageId());
          }
        }, 2000);
        command.execute(msg);
        return;
      }

      String errMsg = "*请登陆*";
      if (hasCookie) errMsg = "*您已登陆*";
      Store.context.getBean(Receiver.class).sendMsg(msg.getChatId(), "md", errMsg, null);
    }
  }

}
