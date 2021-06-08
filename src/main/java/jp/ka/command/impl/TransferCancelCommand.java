package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TransferCancelCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    if (U2.transferIds.size() > 0) {
      U2.transferIds.clear();
      receiver.sendMsg(gid, "*队列已清空*", "md");
    } else {
      receiver.sendMsg(gid, "*队列没有任务*", "md");
    }
  }

  @Override
  public CMD cmd() {
    return CMD.TRANSFER_CANCEL;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "取消转账队列";
  }

}
