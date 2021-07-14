package jp.ka.command.impl;

import jp.ka.bean.config.User;
import jp.ka.command.Command;
import jp.ka.mapper.U2Mapper;
import jp.ka.push.FreePush;
import jp.ka.push.PmPush;
import jp.ka.variable.U2Info;
import jp.ka.controller.Receiver;
import jp.ka.utils.HttpUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;

@Component
public class LogoutCommand implements Command {

  @Autowired
  private User user;

  @Autowired
  private Receiver receiver;

  @Autowired
  private U2Mapper mapper;

  @SneakyThrows
  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    HttpUtils.get(gid, "/logout.php?key=" + U2Info.pageKey);
    PmPush.stop();
    FreePush.stop();
    HttpUtils.session.clear();
    user.setId(null);
    mapper.clearInfo();
    mapper.clearCookie();
    receiver.sendMsg(gid, "md", "*登出成功！*", null);
  }

  @Override
  public CMD cmd() {
    return CMD.LOGOUT;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "登出";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
