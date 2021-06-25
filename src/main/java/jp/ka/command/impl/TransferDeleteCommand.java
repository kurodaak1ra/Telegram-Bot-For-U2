package jp.ka.command.impl;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import jp.ka.command.Command;
import jp.ka.variable.MsgTpl;
import jp.ka.controller.Receiver;
import jp.ka.variable.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransferDeleteCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    String[] split = msg.getText().split("\n");
    if (split.length != 2) {
      prompt(gid);
      return;
    }

    String rids = split[1].trim();

    List<Integer> deleted = new ArrayList<>();
    for (String rid : rids.split(" ")) {
      if (rid.trim().equals("")) continue;
      Integer id = Integer.valueOf(rid);
      boolean remove = Store.TRANSFER_LIST.remove(id);
      if (remove) deleted.add(id);
    }

    String tmpMsg = "*未匹配到任何 UID*";
    if (deleted.size() != 0) tmpMsg = String.format("*已删除 UID*\n\n`%s`", String.join("\n", Lists.transform(Store.TRANSFER_LIST, Functions.toStringFunction())));
    receiver.sendMsg(gid, "md", tmpMsg, null);

    if (Store.TRANSFER_LIST.size() == 0)  receiver.sendMsg(gid, "md", "转账任务结束", null);
  }

  @Override
  public CMD cmd() {
    return CMD.TRANSFER_DELETE;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "删除队列中 UID";
  }

  @Override
  public Message prompt(Long gid) {
    return receiver.sendMsg(gid, "md", MsgTpl.COMMAND_ERROR + "\n\n`/transfer_delete`\n`<uid - 可多个空格分隔>`", null);
  }

}
