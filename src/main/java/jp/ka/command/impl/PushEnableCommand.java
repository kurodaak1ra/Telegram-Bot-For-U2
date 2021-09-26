package jp.ka.command.impl;

import jp.ka.callback.Callback;
import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.variable.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

@Component
public class PushEnableCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, "md", "*推送服务控制台*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("PM 提醒"),
        Arrays.asList(Store.PM_PUSH ? "■" : "□", CMD.PUSH_SERVICE + ":pm")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("全站 FREE 提醒"),
        Arrays.asList(Store.FREE_PUSH ? "■" : "□", CMD.PUSH_SERVICE + ":free")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("签到 提醒"),
        Arrays.asList(Store.SIGN_PUSH ? "■" : "□", CMD.PUSH_SERVICE + ":sign")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("❌", Callback.CBK.TORRENT_LINK + ":close")
      ))
    ));
  }

  @Override
  public CMD cmd() {
    return CMD.PUSH_SERVICE;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "推送服务";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
