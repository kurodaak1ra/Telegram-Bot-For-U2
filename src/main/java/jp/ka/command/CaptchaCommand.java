package jp.ka.command;

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

@Slf4j
@Component
public class CaptchaCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Message msg = update.getMessage();
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, Text.WAITING, "md");
    try {
      InputStream pic = HttpUtils.getPic(gid, "/captcha.php?sid=" + Math.random());
      receiver.sendDoc(gid, "登陆验证码", new InputFile().setMedia(pic, "captcha.png"));
      Config.step = CMD.CAPTCHA;
    } catch (HttpException e) { }
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

}
