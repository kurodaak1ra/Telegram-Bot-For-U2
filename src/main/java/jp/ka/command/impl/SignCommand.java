package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.bean.RespGet;
import jp.ka.utils.Store;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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

    Integer mid = (Integer) redis.get(Store.SIGN_MESSAGE_ID_KEY);
    if (Objects.nonNull(mid)) {
      receiver.sendDel(gid, mid);
      redis.del(Store.SIGN_MESSAGE_ID_KEY);
    }

    receiver.sendMsg(gid, "md", Text.WAITING, null);
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
    HashMap<String, String> params = new HashMap<>();
    Map<String, String> options = new HashMap<>();
    Integer mid = (Integer) redis.get(Store.SIGN_MESSAGE_ID_KEY);
    String mark = UUID.randomUUID().toString();
    redis.set(Store.SIGN_MESSAGE_MARK_KEY, mark, Store.TTL);

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
        String uuid = cacheData(mark, "item", params, entry);
        List<List<List<String>>> column = Arrays.asList(Arrays.asList(Arrays.asList(entry.getKey(), CMD.SIGN + ":" + uuid)));
        columns.add(column);
      }
      String refreshUUID = cacheData(mark, "refresh", null, null);
      List<List<List<String>>> refreshColumn = Arrays.asList(Arrays.asList(Arrays.asList("刷 🔄 新", CMD.SIGN + ":" + refreshUUID)));
      columns.add(refreshColumn);

      InputStream pic = HttpUtils.getPic(gid, "/" + img);
      if (Objects.isNull(mid)) {
        InputFile file = new InputFile();
        file.setMedia(pic, "sign pic.png");
        Message message = receiver.sendImg(gid, "", "", file, columns);
        redis.set(Store.SIGN_MESSAGE_ID_KEY, message.getMessageId(), Store.TTL);
      } else {
        InputMedia media = new InputMedia() {
          @Override
          public String getType() {
            return "photo";
          }
        };
        media.setMedia(pic, "sign pic.png");
        receiver.sendEditMedia(gid, mid, media, columns);
      }
    } catch (HttpException e) { }
  }

  private String cacheData(String mark, String source, Map<String, String> params, Map.Entry<String, String> entry) {
    String uuid = UUID.randomUUID().toString();

    Map<String, String> data = new HashMap<>();
    if (Objects.nonNull(params)) data.putAll(params);
    if (Objects.nonNull(entry)) data.put(entry.getValue(), entry.getKey());

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", source);
    map.put("data", data);
    map.put("mark", mark);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

}
