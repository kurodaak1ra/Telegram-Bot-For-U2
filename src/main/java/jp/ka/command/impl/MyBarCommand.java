package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Config;
import jp.ka.variable.U2;
import jp.ka.controller.Receiver;
import jp.ka.utils.HttpUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.InputStream;
import java.util.Arrays;

@Component
public class MyBarCommand implements Command {

  @Autowired
  private Receiver receiver;

  @SneakyThrows
  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    String uri = "/mybar.php?namered=229&namegreen=77&namex=5&nameblue=38&upx=90&upy=3&userid=" + U2.uid + ".png";

    InputStream img = HttpUtils.getPic(gid, uri);
    receiver.sendImg(gid, "", "", new InputFile(img, "mybar.png"), Arrays.asList(Arrays.asList(Arrays.asList(
      Arrays.asList("点击查看原图", Config.U2Domain + uri)
    ))));
  }

  @Override
  public CMD cmd() {
    return CMD.MY_BAR;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "签名条";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
