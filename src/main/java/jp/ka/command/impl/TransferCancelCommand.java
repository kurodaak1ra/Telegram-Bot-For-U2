package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.config.U2;
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
public class TransferCancelCommand implements Command {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, "md", Text.WAITING, null);
    List<Object> list = (List<Object>) redis.get(Store.TRANSFER_DATA_KEY);
    if (Objects.nonNull(list) && list.size() > 0) {
      redis.set(Store.TRANSFER_DATA_KEY, new ArrayList<String>(), -1);
      receiver.sendMsg(gid, "md", "*队列已清空*", null);
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
