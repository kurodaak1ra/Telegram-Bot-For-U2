package jp.ka.command;

import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TransferInfoCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    if (U2.transferIds.size() > 0) {
      receiver.sendMsg(gid, String.format("*当前等待用户*\n\n`%s`", String.join("\n", U2.transferIds)), "md");
    } else {
      receiver.sendMsg(gid, "*没有等待用户*", "md");
    }
  }

  @Override
  public CMD cmd() {
    return CMD.TRANSFER_INFO;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "当前转账队列";
  }

}
