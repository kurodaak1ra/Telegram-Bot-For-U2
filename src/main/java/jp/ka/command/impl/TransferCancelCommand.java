package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;

@Component
public class TransferCancelCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    if (Store.TRANSFER_LIST.size() > 0) {
      Store.TRANSFER_LIST = new ArrayList<>();
      receiver.sendMsg(gid, "md", "*队列已清空*", null);
      receiver.sendMsg(gid, "md", "*转账任务结束*", null);
    } else {
      receiver.sendMsg(gid, "md", "*队列没有任务*", null);
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

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
