package jp.ka.controller;

import jp.ka.bean.config.User;
import jp.ka.command.Command;
import jp.ka.variable.MsgTpl;
import jp.ka.variable.Store;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
public class CommandResolver {

  private final User user;

  private final Map<String, Command> commandMap = new HashMap<>();

  public void initCommandMap(ApplicationContext context) {
    context.getBeansOfType(Command.class).values().forEach(this::putCommands);
  }

  public CommandResolver(User user) {
    this.user = user;
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

      if ((Objects.nonNull(  user.getId()) && (!command.getClass().getSimpleName().equals("CaptchaCommand") && !command.getClass().getSimpleName().equals("LoginCommand"))) || (Objects.isNull(  user.getId()) && !command.needLogin())) {
        String firstLine = msg.getText().toUpperCase().split("\n")[0].trim();
        if (firstLine.contains(" ")) {
          Message prompt = command.prompt(msg.getChatId());
          if (Objects.nonNull(prompt)) return;
        }
        Message waiting = Store.context.getBean(Receiver.class).sendMsg(msg.getChatId(), "md", MsgTpl.WAITING, null);
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
      if (Objects.nonNull(  user.getId())) errMsg = "*您已登陆*";
      Store.context.getBean(Receiver.class).sendMsg(msg.getChatId(), "md", errMsg, null);
    }
  }

}
