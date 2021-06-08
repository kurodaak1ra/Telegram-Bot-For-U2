package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransferDeleteCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    String[] split = update.getMessage().getText().split("\n");
    if (split.length != 2) {
      receiver.sendMsg(gid, Text.COMMAND_ERROR + copyWriting(), "md");
    }

    List<String> tmpList = new ArrayList<>();
    for (String uid : split[1].split(" ")) {
      try {
        Integer id = new Integer(uid);
        boolean remove = U2.transferIds.remove(id);
        if (remove) tmpList.add(uid);
      } catch (NumberFormatException e) {}
    }

    String tmpMsg = "";
    if (tmpList.size() == 0) {
      tmpMsg = "*未匹配到任何 UID*";
    } else {
      tmpMsg = String.format("*已删除 UID*\n\n`%s`", String.join("\n", tmpList));
    }
    receiver.sendMsg(gid, tmpMsg, "md");
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
    return "删除队列中一个或多个 UID";
  }

  private String copyWriting() {
    return "\n\n`/transfer_delete`\n`<uid - 可多个空格分隔>`";
  }

}
