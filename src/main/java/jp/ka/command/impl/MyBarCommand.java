package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MyBarCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, "md", Text.WAITING, null);
    try {
      InputStream img = HttpUtils.getPic(gid, "/mybar.php?namered=229&namegreen=77&namex=5&nameblue=38&upx=90&upy=3&userid="+ U2.uid +".png");

      List<List<List<List<String>>>> columns = Arrays.asList(Arrays.asList(Arrays.asList(Arrays.asList("点击查看原图", Config.U2Domain + "/mybar.php?namered=229&namegreen=77&namex=5&nameblue=38&upx=90&upy=3&userid=" + U2.uid + ".png"))));

      receiver.sendImg(gid, "", new InputFile(img, "mybar.png"), columns);
    } catch (HttpException e) { }
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
    return "信息条";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

}
