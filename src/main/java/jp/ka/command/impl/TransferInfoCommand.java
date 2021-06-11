package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Objects;

@Component
public class TransferInfoCommand implements Command {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    List<Object> list = (List<Object>) redis.get(Store.TRANSFER_DATA_KEY);
    List<String> listIds = (List<String>)(List)list;
    if (Objects.nonNull(list) && list.size() > 0) {
      receiver.sendMsg(gid, "md", String.format("*当前等待用户*\n\n`%s`", String.join("\n", listIds)), null);
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

}
