package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.bean.RespGet;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.InputStream;
import java.util.*;

@Component
public class SignCommand implements Command {

  @Autowired
  private Receiver receiver;

  @Autowired
  private RedisUtils redis;

  @Override
  public void execute(Update update) {
    Long gid = update.getMessage().getChatId();

    String img = "";
    HashMap<String, String> params = new HashMap<>();
    Map<String, String> options = new HashMap<>();

    Object obj = redis.get("sign.message.id");
    if (Objects.nonNull(obj)) {
      receiver.delMsg(gid, (Integer)obj);
    }
    receiver.sendMsg(gid, Text.WAITING, "md");
    try {
      RespGet resp = HttpUtils.get(gid, "/showup.php");
      Elements elements = resp.getHtml().getElementsByClass("captcha");
      if (elements.size() == 0) {
        receiver.sendMsg(gid, "*今天已签到*", "md");
        return;
      }

      Element captcha = elements.get(0);
      Elements imgs = captcha.getElementsByTag("img");
      Elements btns = captcha.getElementsByTag("input");

      img = imgs.get(0).attr("src");
      params.put("message", "来自 Telegram Bot");
      params.put("req", btns.get(0).attr("value"));
      params.put("hash", btns.get(1).attr("value"));
      params.put("form", btns.get(2).attr("value"));
      for (int i = 3; i < btns.size(); i++) {
        options.put(btns.get(i).attr(("value")), btns.get(i).attr(("name")));
      }
    } catch (HttpException e) {
      return;
    }

    try {
      int ttl = 5 * 60;
      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> botBtnList = new ArrayList<>();
      for (Map.Entry<String, String> entry : options.entrySet()) {
        String uuid = UUID.randomUUID().toString();
        HashMap<String, String> tmpMap = new HashMap<>();
        tmpMap.putAll(params);
        tmpMap.put(entry.getValue(), entry.getKey());
        redis.set(uuid, tmpMap, ttl);

        List<InlineKeyboardButton> tmpBtnList = new ArrayList<>();
        InlineKeyboardButton tmpBtn = new InlineKeyboardButton();
        tmpBtn.setText(entry.getKey());
        tmpBtn.setCallbackData(CMD.SIGN + ":" + uuid);
        tmpBtnList.add(tmpBtn);
        botBtnList.add(tmpBtnList);
      }
      markup.setKeyboard(botBtnList);

      InputStream pic = HttpUtils.getPic(gid, "/" + img);
      Message message = receiver.sendImg(gid, "", new InputFile().setMedia(pic, "sign pic.png"), markup);
      redis.set("sign.message.id", message.getMessageId(), ttl);
    } catch (HttpException e) { }
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

}
