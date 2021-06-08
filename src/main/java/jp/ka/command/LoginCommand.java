package jp.ka.command;

import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.bean.RespPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Component
public class LoginCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Message msg = update.getMessage();
    Long gid = msg.getChatId();
    Integer mid = msg.getMessageId();

    if (Objects.isNull(Config.step) || !Config.step.equals(CMD.CAPTCHA)) {
      receiver.sendMsg(gid, "*登陆前请先获取验证码*", "md");
      return;
    }

    String[] split = msg.getText().split("\n");
    if (split.length != 4) {
      receiver.sendMsg(gid, Text.COMMAND_ERROR + copyWriting(), "md");
      return;
    }

    receiver.delMsg(gid, mid);
    receiver.sendMsg(gid, Text.WAITING, "md");
    try {
      ArrayList<NameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("login_type", "email"));
      params.add(new BasicNameValuePair("login_ajax", "1"));
      params.add(new BasicNameValuePair("username", split[1].trim()));
      params.add(new BasicNameValuePair("password", split[2].trim()));
      params.add(new BasicNameValuePair("captcha", split[3].trim()));

      RespPost resp = HttpUtils.postForm(gid, "/takelogin.php", params);
      if (resp.getData().get("status").equals("error")) {
        String errMsg = "";
        switch (resp.getData().get("message")) {
          case "Wrong CAPTCHA answer.": {
            errMsg = "验证码错误";
            break;
          }
          case "Unsuccessful login, please try again.": {
            errMsg = "帐号或密码错误";
            break;
          }
          default:
            errMsg = resp.getData().get("message");
        }
        receiver.sendMsg(gid, String.format("*登陆失败:* `%s`", errMsg), "md");
        return;
      }
      Config.uid = msg.getFrom().getId();
      Config.step = null;
      receiver.sendMsg(gid, "*登陆成功*", "md");
      ToolsCommand.setUserData(gid);
    } catch (HttpException e) { }
  }

  @Override
  public CMD cmd() {
    return CMD.LOGIN;
  }

  @Override
  public Boolean needLogin() {
    return false;
  }

  @Override
  public String description() {
    return "登陆";
  }

  private String copyWriting() {
    return "\n\n`/login`\n`<user@example\\.com>`\n`<password>`\n`<captcha code>`";
  }

}
