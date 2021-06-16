package jp.ka.command.impl;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class TransferInfoCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    if (Store.TRANSFER_LIST.size() > 0) {
      receiver.sendMsg(gid, "md", String.format("*当前等待用户*\n\n`%s`", String.join("\n", Lists.transform(Store.TRANSFER_LIST, Functions.toStringFunction()))), null);
    } else {
      receiver.sendMsg(gid, "md", "*没有等待用户*", null);
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

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
