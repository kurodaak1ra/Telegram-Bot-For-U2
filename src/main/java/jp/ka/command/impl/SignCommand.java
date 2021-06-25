package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.bean.RespGet;
import jp.ka.variable.Store;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

import java.io.InputStream;
import java.util.*;

@Component
public class SignCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Autowired
  private RedisUtils redis;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    if (Store.SIGN_MESSAGE_ID != -1) {
      receiver.sendDel(gid, Store.SIGN_MESSAGE_ID);
      Store.SIGN_MESSAGE_ID = -1;
    }

    sendSign(gid);
  }

  @Override
  public CMD cmd() {
    return CMD.SIGN;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "签到";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

  public void sendSign(Long gid) {
    String img = "";
    Map<String, String> params = new HashMap<>();
    Map<String, String> options = new HashMap<>();
    String mark = UUID.randomUUID().toString();
    Store.SIGN_MESSAGE_MARK = mark;

    try {
      RespGet resp = HttpUtils.get(gid, "/showup.php");
      Elements elements = resp.getHtml().getElementsByClass("captcha");
      if (elements.size() == 0) {
        receiver.sendMsg(gid, "md", "*今天已签到*", null);
        return;
      }

      Element captcha = elements.get(0);
      Elements imgs = captcha.getElementsByTag("img");
      Elements btns = captcha.getElementsByTag("input");

      img = imgs.get(0).attr("src");
      params.put("message", Store.ADV);
      params.put("req", btns.get(0).attr("value"));
      params.put("hash", btns.get(1).attr("value"));
      params.put("form", btns.get(2).attr("value"));
      for (int i = 3; i < btns.size(); i++) options.put(btns.get(i).attr(("value")), btns.get(i).attr(("name")));
    } catch (HttpException e) {
      return;
    }

    try {
      List<List<List<List<String>>>> columns = new ArrayList<>();
      for (Map.Entry<String, String> entry : options.entrySet()) {
        columns.add(Arrays.asList(Arrays.asList(
          Arrays.asList(entry.getKey(), CMD.SIGN + ":" + cacheData(mark, "item", params, entry))
        )));
      }
      columns.add(Arrays.asList(Arrays.asList(
        Arrays.asList("刷 🔄 新", CMD.SIGN + ":" + cacheData(mark, "refresh", null, null))
      )));

      InputStream pic = HttpUtils.getPic(gid, "/" + img);
      if (Store.SIGN_MESSAGE_ID == -1) {
        InputFile file = new InputFile();
        file.setMedia(pic, "sign pic.png");
        Message msg = receiver.sendImg(gid, "", "", file, columns);
        Store.SIGN_MESSAGE_ID = msg.getMessageId();
      } else {
        InputMedia media = new InputMedia() {
          @Override
          public String getType() {
            return "photo";
          }
        };
        media.setMedia(pic, "sign pic.png");
        receiver.sendEditMedia(gid, Store.SIGN_MESSAGE_ID, media, columns);
      }
    } catch (HttpException e) { }
  }

  private String cacheData(String mark, String source, Map<String, String> params, Map.Entry<String, String> entry) {
    String uuid = UUID.randomUUID().toString();

    Map<String, String> data = new HashMap<>();
    if (Objects.nonNull(params)) data.putAll(params);
    if (Objects.nonNull(entry)) data.put(entry.getValue(), entry.getKey());

    Map<String, Object> map = new HashMap<>();
    map.put("source", source);
    map.put("data", data);
    map.put("mark", mark);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

}
