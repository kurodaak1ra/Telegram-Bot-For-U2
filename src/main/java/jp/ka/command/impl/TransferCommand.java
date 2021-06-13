package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.bean.RespPost;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
public class TransferCommand implements Command {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  private final int TTL = -1;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    String[] split = msg.getText().split("\n");
    if (split.length < 3 || split.length > 4) {
      prompt(gid);
      return;
    }

    for (String id : split[1].trim().split(" ")) {
      String tmpID = id.trim();
      if (!tmpID.equals("")) redis.lpush(Store.TRANSFER_DATA_KEY, id, TTL);
    }
    String message = "";
    if (split.length == 4) message = split[3].trim();

    receiver.sendMsg(gid, "md", Text.WAITING, null);
    Boolean mark = (Boolean) redis.get(Store.TRANSFER_MARK_KEY);
    if (Objects.isNull(mark) || !mark) {
      queue(gid, split[2].trim(), message);
      redis.set(Store.TRANSFER_MARK_KEY, true, TTL);
      transferCoin(gid, (String) redis.lunshift(Store.TRANSFER_DATA_KEY), split[2].trim(), message);
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
    return receiver.sendMsg(gid, "md", Text.COMMAND_ERROR + "\n\n`/transfer`\n`<uid - 可多个空格分隔>`\n`<count - 数量>`\n`<message - 留言 (可省略)>`", null);
  }

  private void transferCoin(Long gid, String recvID, String amount, String message) {
    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("event", "1003"));
    params.add(new BasicNameValuePair("recv", recvID));
    params.add(new BasicNameValuePair("amount", amount));
    params.add(new BasicNameValuePair("message", message));

    try {
      RespPost resp = HttpUtils.postForm(gid, "/mpshop.php", params);
      if (resp.getCode() == 200) {
        Element embedded = resp.getHtml().getElementsByClass("embedded").get(1);
        String text = embedded.getElementsByClass("text").get(0).text();

        if (text.contains("请勿进行频繁转账")) {
          redis.lshift(Store.TRANSFER_DATA_KEY, recvID, TTL);
          receiver.sendMsg(gid, "md", "*追加任务成功*", null);
        } else {
          receiver.sendMsg(gid, "md", String.format("*%s*", text), null);
        }
      } else if (resp.getCode() == 302) {
        receiver.sendMsg(gid, "md", String.format("*[%s] 转账成功*", recvID), null);
      }
    } catch (HttpException e) { }
  }

  private void queue(Long gid, String amount, String message) {
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        List<Integer> list = (List<Integer>) redis.get(Store.TRANSFER_DATA_KEY);
        if (Objects.nonNull(list) && list.size() > 0) transferCoin(gid, (String) redis.lunshift(Store.TRANSFER_DATA_KEY), amount, message);
        if (Objects.nonNull(list) && list.size() > 0) queue(gid, amount, message);
        if (Objects.nonNull(list) && list.size() == 0) {
          redis.set(Store.TRANSFER_MARK_KEY, false, TTL);
          receiver.sendMsg(gid, "md", "转账任务结束", null);
        }
      }
    }, 5 * 60 * 1000 + 1000); // 五分零一秒
  }

}
