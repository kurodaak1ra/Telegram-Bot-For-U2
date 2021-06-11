package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.callback.CallbackTools;
import jp.ka.command.impl.SignCommand;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.bean.RespGet;
import jp.ka.utils.Store;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@Component
public class SignCallback implements Callback {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Update update) {
    CallbackQuery query = update.getCallbackQuery();
    Long gid = query.getMessage().getChatId();

    Map<String, Object> cache = CallbackTools.hasExpired(gid, query);
    if (Objects.isNull(cache)) return;

    String mark = (String) cache.get("mark");
    String cacheMark = (String) redis.get(Store.SIGN_MESSAGE_MARK_KEY);
    if (!mark.equals(cacheMark)) {
      receiver.sendDel(gid, query.getMessage().getMessageId());
      receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_EXPIRE);
      return;
    }

    String source = (String) cache.get("source");
    switch (source) {
      case "item": {
        receiver.sendDel(gid, query.getMessage().getMessageId());
        receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_WAITING);
        Map<String, String> data = (Map<String, String>) cache.get("data");
        item(gid, data);
        break;
      }
      case "refresh": {
        receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_REFRESH);
        refresh(gid);
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.SIGN;
  }

  private void item(Long gid, Map<String, String> data) {
    try {
      ArrayList<NameValuePair> params = new ArrayList<>();
      for (Map.Entry<String, String> entry : data.entrySet()) {
        params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
      }
      HttpUtils.postForm(gid, "/showup.php?action=show", params);
    } catch (HttpException e) {
      return;
    }

    try {
      RespGet resp = HttpUtils.get(gid, "/showup.php");
      Element table = resp.getHtml().getElementsByTag("table").get(10);
      Elements td = table.getElementsByTag("td");
      String res = td.get(2).getElementsByTag("fieldset").get(0).getElementsByTag("span").get(0).text().replaceAll("\\(", "").replaceAll("\\)", "").substring(0, 4);
      String uc = td.get(1).getElementsByTag("b").get(3).text();

      receiver.sendMsg(gid, "md", String.format("*%s*\n*奖励UCoin*: `%s`", res, uc), null);
    } catch (HttpException e) { }
  }

  private void refresh(Long gid) {
    Store.context.getBean(SignCommand.class).sendSign(gid);
  }

}
