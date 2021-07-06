package jp.ka.push;

import jp.ka.bean.RespGet;
import jp.ka.bean.config.U2;
import jp.ka.bean.config.User;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.variable.MsgTpl;
import jp.ka.variable.Store;
import jp.ka.variable.U2Info;
import lombok.SneakyThrows;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FreePush {

  private static U2 u2;
  private static User user;

  @Autowired
  public void setU2(U2 u2) {
    this.u2 = u2;
  }
  @Autowired
  public void setUser(User user) {
    this.user = user;
  }

  private static Timer timer;
  private static TimerTask timerTask;

  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

  static {
    SDF.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  public static void start() {
    if (Objects.isNull(user.getId()) || Objects.nonNull(timerTask)) return;
    Store.FREE_PUSH = true;
    timerTask = new TimerTask() {
      @Override
      public void run() {
        free();
      }
    };

    timer = new Timer("free_push");
    // timer.purge();
    timer.schedule(timerTask, 0, Store.PUSH_INTERVAL_TIME);
  }

  public static void stop() {
    if (Objects.isNull(timerTask)) return;
    Store.FREE_PUSH = false;
    timerTask.cancel();
    timerTask = null;
    timer = null;
  }

  private static void free() {
    RespGet resp = null;
    try {
      resp = HttpUtils.get(user.getId(), "/promotion.php?action=list&page=0");
      Elements tables = resp.getHtml().getElementById("outer").getElementsByTag("table");
      Element content = tables.get(tables.size() - 1);

      Elements rows = content.child(0).children();
      for (int i = 1; i < rows.size(); i++) {
        Elements columns = rows.get(i).children();
        String fid = columns.get(0).text();
        String type = columns.get(1).text();
        String torrent = columns.get(2).text();
        String to = columns.get(3).text();
        String status = columns.get(5).text();

        if (Store.FREE_INFO.containsKey(fid)) {
          Map<String, String> val = Store.FREE_INFO.get(fid);
          long difference = SDF.parse(val.get("end_time") + " +0800").getTime() - new Date().getTime();
          if (difference <= 0) {
            Store.FREE_INFO.remove(fid);
            Store.context.getBean(Receiver.class).sendMsg(user.getId(), "md", String.format("*全站 FREE 到期提醒*\n\n魔法: [\\#%s](%s/promotion.php?action=detail&id=%s)\n创建: [%s](%s/userdetails.php?id=%s)\n开始: `%s`\n结束: `%s`\n类型: `%s`\n备注: `%s`",
            fid, u2.getDomain(), fid, val.get("cname"), u2.getDomain(), val.get("cid"), val.get("create_time"), val.get("end_time"), val.get("rate"), val.get("remarks")), null);
          }
          continue;
        }

        if (type.equals("魔法") && torrent.equals("全局") && to.equals("所有人") && status.equals("有效")) freeNotice(fid);
      }
      Store.FREE_PUSH_REQ_FAILED_TIMES = 0;
    } catch (HttpException | ParseException e) {
      if (resp.getCode() >= 500) {
        if (Store.FREE_PUSH_REQ_FAILED_TIMES >= 10) {
          stop();
          Store.FREE_PUSH_REQ_FAILED_TIMES = 0;
          Store.context.getBean(Receiver.class).sendMsg(user.getId(), "md", String.format(MsgTpl.PUSH_FAILED_MULTIPLE_TIMES, "FREE"), null);
        } else Store.FREE_PUSH_REQ_FAILED_TIMES++;
      }
    }
  }

  @SneakyThrows
  private static void freeNotice(String fid) {
    RespGet resp = HttpUtils.get(user.getId(), "/promotion.php?action=detail&id=" + fid);
    Elements tables = resp.getHtml().getElementById("outer").getElementsByTag("table");
    Element content = tables.get(tables.size() - 1);

    Elements rows = content.child(0).children();
    Element creator = rows.get(3).child(1).getElementsByTag("a").get(0);
    String cid = creator.attr("href").split("id=")[1];
    String cname = creator.text();

    String createTime = rows.get(3).child(1).getElementsByTag("time").attr("title");
    String endTime = rows.get(5).child(1).getElementsByTag("time").text();

    Element _rate = rows.get(6).child(1);
    String rate = _rate.child(0).attr("alt");
    String rateUp = null;
    String rateDown = null;
    if (rate.equals(U2Info._PROMOTION)) {
      rateUp = _rate.child(2).text();
      rateDown = _rate.child(4).text();
    }

    List<TextNode> mark = rows.get(7).child(1).getElementsByTag("fieldset").get(0).textNodes();
    String remarks = mark.size() == 0 ? "" : mark.get(0).text();

    HashMap<String, String> map = new HashMap<>();
    map.put("fid", fid);
    map.put("cid", cid);
    map.put("cname", CommonUtils.formatMD(cname));
    map.put("create_time", createTime);
    map.put("end_time", endTime);
    map.put("rate", CommonUtils.torrentStatus(rate, rateUp, rateDown));
    map.put("remarks", remarks);
    Store.FREE_INFO.put(fid, map);

    Store.context.getBean(Receiver.class).sendMsg(user.getId(), "md", String.format("*全站 FREE 提醒*\n\n魔法: [\\#%s](%s/promotion.php?action=detail&id=%s)\n创建: [%s](%s/userdetails.php?id=%s)\n开始: `%s`\n结束: `%s`\n类型: `%s`\n备注: `%s`",
      fid, u2.getDomain(), fid, CommonUtils.formatMD(cname), u2.getDomain(), cid, createTime, endTime, CommonUtils.torrentStatus(rate, rateUp, rateDown), remarks), null);
  }

}
