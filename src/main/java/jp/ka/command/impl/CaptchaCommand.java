package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.InputStream;
import java.util.Arrays;

@Component
public class CaptchaCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, "md", Text.WAITING, null);
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

  public void sendCaptcha(Long gid) {
    if (Config.session.containsKey(Config.cookieKey)) return;

    try {
      InputStream pic = HttpUtils.getPic(gid, "/captcha.php?sid=" + Math.random());
      receiver.sendDoc(gid, "", "", new InputFile().setMedia(pic, "captcha.png"), Arrays.asList(Arrays.asList(Arrays.asList(
        Arrays.asList("刷 🔄 新", CMD.CAPTCHA + ":refresh")
      ))));
      Config.step = CMD.CAPTCHA;
    } catch (HttpException e) { }
  }

}
