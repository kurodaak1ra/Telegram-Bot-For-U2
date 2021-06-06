package jp.ka.command;

import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class MeCommand extends Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Message msg = update.getMessage();
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, Text.WAITING, "md", -1);

    ToolsCommand.setUserData();

    String tmpMsg = String.format("*个人信息*\n\n__UID__: `%s`\n__用户名__: `%s`\n__分享率__: `%s`\n__上传量__: `%s`\n__下载量__: `%s`\n__UCoin__: `%s/%s/%s`\n__邀请__: `%s`\n__客户端__: `%s`\n__上传__: `%s`\n__下载__: `%s`",
        U2.uid, U2.username, U2.shareRate, U2.uploads, U2.downloads, U2.coinGold, U2.coinSilver, U2.coinCopper, U2.invite, U2.client, U2.uploading, U2.downloading);
    receiver.sendMsg(gid, tmpMsg, "md", -1);
  }

  @Override
  public CMD cmd() {
    return CMD.ME;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "个人信息";
  }

}
