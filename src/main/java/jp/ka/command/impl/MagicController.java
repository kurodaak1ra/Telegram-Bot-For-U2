package jp.ka.command.impl;

import jp.ka.bean.RespGet;
import jp.ka.bean.RespPost;
import jp.ka.command.Command;
import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;

@Component
public class MagicController implements Command {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    String[] split = msg.getText().split("\n");
    if (split.length != 5) {
      prompt(gid);
      return;
    }

    String tid = split[1].trim();
    String uid = split[2].toUpperCase().trim();
    String hours = split[3].trim();
    String promotion = split[4].trim();
    String[] promotions = promotion.split(" ");
    String promote = promotions[0];
    if (promote.equals("8") && promotions.length != 3) {
      prompt(gid);
      return;
    }
    String ur = "1.00";
    String dr = "1.00";
    if (promotions.length == 3) {
      ur = promotions[1];
      dr = promotions[2];
    }

    HashMap<String, Object> magic = magicPre(gid, tid, uid, hours, promote, ur, dr);
    List<NameValuePair> params = (List<NameValuePair>) magic.get("params");
    Map<String, String> fee = (Map<String, String>) magic.get("fee");

    receiver.sendMsg(gid, "md", String.format("*预计费用*: `%s%s%s`",
      fee.get("gold").equals("") ? "" : "\uD83E\uDD47" + fee.get("gold"),
      fee.get("silver").equals("") ? "" : "\uD83E\uDD48" + fee.get("silver"),
      fee.get("copper").equals("") ? "" : "\uD83E\uDD49" + fee.get("copper")
    ), Arrays.asList(
      Arrays.asList(Arrays.asList(Arrays.asList("施放魔法", CMD.MAGIC + ":" + cacheData("params", params)))),
      Arrays.asList(Arrays.asList(Arrays.asList("❌", CMD.MAGIC + ":" + cacheData("close", null))))
    ));
  }

  @Override
  public CMD cmd() {
    return CMD.MAGIC;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "施放魔法";
  }

  @Override
  public Message prompt(Long gid) {
    return receiver.sendMsg(gid, "md", Text.COMMAND_ERROR + "\n\n`/magic`\n`tid - 种子 ID`\n`uid - 用户 ID (ALL 全局)`\n`hours - 时限`\n`promotion - \\{\n  2: Free\n  3: 2X\n  4: 2X Free\n  5: 50%\n  6: 2X 50%\n  7: 30%\n  8: Custom (8 1.00 2.00)\n\\}`", null);
  }

  private String cacheData(String source, List<NameValuePair> params) {
    String uuid = UUID.randomUUID().toString();

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", source);
    if (Objects.nonNull(params)) map.put("params", params);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

  public HashMap<String, Object> magicPre(Long gid, String tid, String uid, String hours, String promote, String ur, String dr) {
    HashMap<String, Object> map = new HashMap<>();

    List<NameValuePair> params = new ArrayList<>();
    try {
      RespGet page = HttpUtils.get(gid, "/promotion.php?action=magic&torrent=" + tid);
      Elements forms = page.getHtml().getElementsByTag("form");
      if (forms.size() == 2) {
        String errText = page.getHtml().getElementsByClass("embedded").get(2).getElementsByTag("td").get(1).text();
        receiver.sendMsg(gid, "md", CommonUtils.formatMD(errText), null);
        return null;
      }
      Elements hiddenInput = forms.get(2).getElementsByTag("input");
      for (int i = 0; i < 8; i++) {
        Element param = hiddenInput.get(i);
        params.add(new BasicNameValuePair(param.attr("name"), param.attr("value")));
      }
    } catch (HttpException e) {
      return null;
    }

    params.add(new BasicNameValuePair("user", uid.equals("ALL") ? uid : (uid.equals(U2.uid) ? "SELF" : "OTHER")));
    params.add(new BasicNameValuePair("user_other", !uid.equals("ALL") && !uid.equals(U2.uid) ? uid : ""));
    params.add(new BasicNameValuePair("start", "0"));
    params.add(new BasicNameValuePair("hours", hours));
    params.add(new BasicNameValuePair("promotion", promote));
    params.add(new BasicNameValuePair("ur", ur));
    params.add(new BasicNameValuePair("dr", dr));
    params.add(new BasicNameValuePair("comment", Store.ADV.replace("\n", " ")));
    map.put("params", params);

    try {
      RespPost resp = HttpUtils.postForm(gid, "/promotion.php?test=1", params);
      if (resp.getData().get("status").equals("error")) {
        receiver.sendMsg(gid, "md", CommonUtils.formatMD(resp.getData().get("error")), null);
        return null;
      }
      Document price = Jsoup.parse(resp.getData().get("price"), "UTF-8");
      HashMap<String, String> feeMap = new HashMap<>();
      feeMap.put("gold", price.getElementsByClass("ucoin-gold").text());
      feeMap.put("silver", price.getElementsByClass("ucoin-silver").text());
      feeMap.put("copper", price.getElementsByClass("ucoin-copper").text());
      map.put("fee", feeMap);
    } catch (HttpException e) {
      return null;
    }

    return map;
  }

}
