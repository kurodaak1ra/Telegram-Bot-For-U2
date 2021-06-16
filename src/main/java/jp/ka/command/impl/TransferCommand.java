package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.utils.HttpUtils;
import jp.ka.bean.RespPost;
import jp.ka.utils.Store;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;

@Component
public class TransferCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    String[] split = msg.getText().split("\n");
    if (split.length < 3 || split.length > 4) {
      prompt(gid);
      return;
    }

    String rids = split[1].trim();
    String count = split[2].trim();
    String message = Store.ADV;
    if (split.length == 4) message = split[3].trim() + "\n\n" + message;

    Integer amount = Integer.valueOf(count);
    for (String rid : rids.split(" ")) {
      if (rid.trim().equals("")) continue;
      Integer id = Integer.valueOf(rid);
      Store.TRANSFER_LIST.add(id);
    }

    if (!Store.TRANSFER_MARK) {
      Store.TRANSFER_MARK = true;
      queue(gid, amount, message);
      transfer(gid, Store.TRANSFER_LIST.remove(0), amount, message);
    } else receiver.sendMsg(gid, "md", "*追加任务成功*", null);
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

  @Override
  public Message prompt(Long gid) {
    return receiver.sendMsg(gid, "md", Text.COMMAND_ERROR + "\n\n`/transfer`\n`<uid - 可多个，空格分隔>`\n`<amount - 转账数量>`\n`<message - 留言 (可省略)>`", null);
  }

  @SneakyThrows
  private void transfer(Long gid, Integer rid, Integer amount, String message) {
    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("event", "1003"));
    params.add(new BasicNameValuePair("recv", rid.toString()));
    params.add(new BasicNameValuePair("amount", amount.toString()));
    params.add(new BasicNameValuePair("message", message));

    RespPost resp = HttpUtils.postForm(gid, "/mpshop.php", params);
    if (resp.getCode() == 200) {
      Element embedded = resp.getHtml().getElementsByClass("embedded").get(1);
      String text = embedded.getElementsByClass("text").get(0).text();

      if (text.contains("请勿进行频繁转账")) {
        Store.TRANSFER_LIST.add(0, rid);
        receiver.sendMsg(gid, "md", "*追加任务成功*", null);
      } else {
        receiver.sendMsg(gid, "md", String.format("*%s*", text), null);
      }
    } else if (resp.getCode() == 302) {
      receiver.sendMsg(gid, "md", String.format("*[%s] 转账成功*", rid), null);
      if (Store.TRANSFER_LIST.size() == 0) receiver.sendMsg(gid, "md", "*转账任务结束*", null);
    }
  }

  private void queue(Long gid, Integer amount, String message) {
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (Store.TRANSFER_LIST.size() > 0) {
          transfer(gid, Store.TRANSFER_LIST.remove(0), amount, message);
          queue(gid, amount, message);
        } else Store.TRANSFER_MARK = false;
      }
    }, 5 * 60 * 1000 + 1000); // 五分零一秒
  }

}
