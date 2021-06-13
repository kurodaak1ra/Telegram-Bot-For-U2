package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TransferDeleteCommand implements Command {

  @Autowired
  private RedisUtils redis;

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

    receiver.sendMsg(gid, "md", Text.WAITING, null);
    List<String> tmpList = new ArrayList<>();
    for (String uid : split[1].split(" ")) {
      Boolean rm = redis.lremove(Store.TRANSFER_DATA_KEY, uid);
      if (Objects.nonNull(rm) && rm) tmpList.add(uid);
    }

    String tmpMsg = "";
    if (tmpList.size() == 0) {
      tmpMsg = "*未匹配到任何 UID*";
    } else {
      tmpMsg = String.format("*已删除 UID*\n\n`%s`", String.join("\n", tmpList));
    }
    receiver.sendMsg(gid, "md", tmpMsg, null);
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

  @Override
  public Message prompt(Long gid) {
    return receiver.sendMsg(gid, "md", Text.COMMAND_ERROR + "\n\n`/transfer_delete`\n`<uid - 可多个空格分隔>`", null);
  }

}
