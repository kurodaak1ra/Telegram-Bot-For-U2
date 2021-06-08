package jp.ka.command;

import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.bean.RespPost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class TransferCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    Message msg = update.getMessage();
    Long gid = msg.getChatId();

    String[] split = msg.getText().split("\n");
    if (split.length < 3 || split.length > 4) {
      receiver.sendMsg(gid, Text.COMMAND_ERROR + copyWriting(), "md");
      return;
    }

    String[] tmpIds = split[1].trim().split(" ");
    for (String id : tmpIds) U2.transferIds.add(id);
    String message = "";
    if (split.length == 4) message = split[3].trim();

    receiver.sendMsg(gid, Text.WAITING, "md");
    boolean firstTransfer = false;
    if (!U2.transferMark) {
      firstTransfer = transferCoin(gid, U2.transferIds.remove(0), split[2].trim(), message);
    } else {
      receiver.sendMsg(gid, "*追加任务成功*", "md");
    }
    if (firstTransfer) {
      U2.transferMark = true;
      queue(gid, split[2].trim(), message);
    }
  }

  @Override
  public CMD cmd() {
    return CMD.TRANSFER;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "金币转账";
  }

  private String copyWriting() {
    return "\n\n`/transfer`\n`<uid - 可多个空格分隔>`\n`<count - 数量>`\n`<message - 留言（可省略）>`";
  }

  private boolean transferCoin(Long gid, String recvID, String amount, String message) {
    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("event", "1003"));
    params.add(new BasicNameValuePair("recv", recvID));
    params.add(new BasicNameValuePair("amount", amount));
    params.add(new BasicNameValuePair("message", message));

    try {
      RespPost resp = HttpUtils.postForm(gid, "/mpshop.php", params);
      if (resp.getCode() == 200) {
        Element embedded = resp.getHtml().getElementsByClass("embedded").get(1);
        Element text = embedded.getElementsByClass("text").get(0);
        receiver.sendMsg(gid, String.format("*%s*", text.text()), "md");
      } else if (resp.getCode() == 302) {
        receiver.sendMsg(gid, String.format("*[%s] 转账成功*", recvID), "md");
      }
      return true;
    } catch (HttpException e) { }
    return false;
  }

  private void queue(Long gid, String amount, String message) {
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (U2.transferIds.size() > 0) transferCoin(gid, U2.transferIds.remove(0), amount, message);
        if (U2.transferIds.size() > 0) queue(gid, amount, message);
        if (U2.transferIds.size() == 0) U2.transferMark = false;
      }
    }, 301000);
  }

}
