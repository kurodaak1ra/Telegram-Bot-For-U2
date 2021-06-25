package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.utils.HttpUtils;
import jp.ka.variable.Store;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.InputStream;
import java.util.Arrays;

@Component
public class CaptchaCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    if (Store.CAPTCHA_MESSAGE_ID != -1) receiver.sendDel(gid, Store.CAPTCHA_MESSAGE_ID);
    sendCaptcha(gid);
  }

  @Override
  public CMD cmd() {
    return CMD.CAPTCHA;
  }

  @Override
  public Boolean needLogin() {
    return false;
  }

  @Override
  public String description() {
    return "登陆验证码";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

  @SneakyThrows
  public void sendCaptcha(Long gid) {
    InputStream pic = HttpUtils.getPic(gid, "/captcha.php?sid=" + Math.random());
    Message msg = receiver.sendDoc(gid, "", "", new InputFile().setMedia(pic, "captcha.png"), Arrays.asList(Arrays.asList(Arrays.asList(
      Arrays.asList("刷 🔄 新", CMD.CAPTCHA + ":refresh")
    ))));
    Store.CAPTCHA_MESSAGE_ID = msg.getMessageId();
    Store.STEP = CMD.CAPTCHA;
  }

}
