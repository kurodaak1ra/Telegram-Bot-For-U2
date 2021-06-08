package jp.ka.callback;

import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.bean.RespGet;
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
    Object form = redis.get(query.getData().split(":")[1]);

    receiver.delMsg(gid, query.getMessage().getMessageId());
    if (Objects.isNull(form)) {
      receiver.sendCallbackAnswer(query.getId(), "签到已过期，请重新获取");
      return;
    }
    receiver.sendCallbackAnswer(query.getId(), "正在提交，请稍等");
    try {
      ArrayList<NameValuePair> params = new ArrayList<>();
      for (Map.Entry<String, String> entry : ((Map<String, String>) form).entrySet()) {
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

      receiver.sendMsg(gid, String.format("*%s*\n*奖励UCoin*: `%s`", res, uc), "md");
    } catch (HttpException e) { }
  }

  @Override
  public CBK cbk() {
    return CBK.SIGN;
  }

}
