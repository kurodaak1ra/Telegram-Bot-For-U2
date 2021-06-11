package jp.ka.callback.impl;

import jp.ka.bean.RespGet;
import jp.ka.callback.Callback;
import jp.ka.callback.CallbackTools;
import jp.ka.command.impl.SearchCommand;
import jp.ka.config.Text;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
public class SearchCallback implements Callback {

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
    String cacheMark = (String) redis.get(Store.SEARCH_MARK_KEY);
    if (!mark.equals(cacheMark)) {
      receiver.sendDel(gid, query.getMessage().getMessageId());
      receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_EXPIRE);
      return;
    }

    List<Map<String, String>> items = (List<Map<String, String>>) redis.get(Store.SEARCH_DATA_KEY);
    Map<String, Object> option = (Map<String, Object>) redis.get(Store.SEARCH_OPTIONS_KEY);
    if (Objects.isNull(items) || Objects.isNull(option)) {
      receiver.sendDel(gid, query.getMessage().getMessageId());
      receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_EXPIRE);
      return;
    }

    Integer torrentLinkMsgID = (Integer) redis.get(Store.TORRENT_LINK_MESSAGE_ID_KEY);
    if (Objects.nonNull(torrentLinkMsgID)) {
      receiver.sendDel(gid, torrentLinkMsgID);
      redis.del(Store.TORRENT_LINK_MESSAGE_ID_KEY);
    }

    redis.expired(Store.SEARCH_DATA_KEY, Store.TTL);
    redis.expired(Store.SEARCH_MARK_KEY, Store.TTL);
    redis.expired(Store.SEARCH_OPTIONS_KEY, Store.TTL);
    redis.expired(Store.SEARCH_MESSAGE_ID_KEY, Store.TTL);

    receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_WAITING);
    String source = (String) cache.get("source");
    Integer page = (Integer) cache.get("page");
    switch (source) {
      case "item": {
        Integer index = (Integer) cache.get("index");
        item(gid, items.get(index));
        break;
      }
      case "prev": {
        prevNext(gid, page);
        break;
      }
      case "next": {
        if (items.size() - (Store.SEARCH_RESULT_COUNT * page) < Store.SEARCH_RESULT_COUNT) {
          int pageSize = (int) option.get("page_size");
          int offset = (int) option.get("offset") + 1;
          if (offset < pageSize) {
            option.put("offset", offset);
            Store.context.getBean(SearchCommand.class).initData(gid);
          }
        }
        prevNext(gid, page);
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.SEARCH;
  }

  private void item(Long gid, Map<String, String> item) {
    try {
      RespGet resp = HttpUtils.get(gid, "/details.php?id=" + item.get("tid") + "&hit=1");
      Elements trs = resp.getHtml().getElementById("outer").getElementsByTag("table").get(0).getElementsByTag("tr");

      StringBuilder sb = new StringBuilder();
      sb.append("`" + item.get("name") + "`\n\n");
      for (int i = 0; i < trs.size(); i++) {
        Element tr = trs.get(i);
        String title = trs.get(i).getElementsByTag("td").get(0).text();
        switch (title) {
          case "下载": {
            Element download = tr.getElementsByTag("a").get(1);
            U2.passKey = download.attr("href").split("&")[1].split("=")[1];
            break;
          }
          case "基本信息": {
            List<TextNode> tn = tr.getElementsByTag("td").get(1).textNodes();
            sb.append("*TID*: `" + item.get("tid") + "` \n");
            sb.append("*大小*: `" + tn.get(2).text().trim() + "` \n");
            sb.append("*类型*: `" + tn.get(3).text().trim() + "` \n");
            Element release = tr.getElementsByTag("time").get(0);
            sb.append("*发布时间*: `" + release.text() + "` \n");
            // System.out.println(release.attr("title"));
            break;
          }
          case "流量优惠": {
            Elements service = tr.getElementsByTag("time");
            if (service.size() == 0) {
              sb.append("*优惠类型*: `普通`\n");
            } else {
              sb.append("*优惠类型*: `" + CommonUtils.torrentStatus(item.get("status"), item.get("status_promotion_upload"), item.get("status_promotion_download")) + "`\n");
              sb.append("*优惠剩余*: `" + service.get(0).text() + "`\n");
              // System.out.println(service.attr("title"));
            }
            break;
          }
          case "活力度": {
            Elements b = tr.getElementsByTag("b");
            if (b.size() == 1) continue;
            String averageProcess = tr.getElementsByTag("td").textNodes().get(1).text().replaceAll("\\(", "").replaceAll("\\)", "").trim();
            sb.append("*平均进度*: `" + averageProcess + "`\n");
            sb.append("*平均速度*: `" + b.get(1).text() + "`\n");
            sb.append("*总速度*: `" + b.get(2).text() + "`");
            break;
          }
        }
      }

      String torrentLinkUUID = cacheData("torrent_link", item.get("tid"));
      List<List<String>> torrentLink = Arrays.asList(Arrays.asList("🔗", CBK.TORRENT_INFO + ":" + torrentLinkUUID));

      String closeUUID = cacheData("close", null);
      List<List<String>> close = Arrays.asList(Arrays.asList("❌", CBK.TORRENT_INFO + ":" + closeUUID));

      List<List<List<String>>> row1 = Arrays.asList(torrentLink);
      List<List<List<String>>> row2 = Arrays.asList(close);
      List<List<List<List<String>>>> columns = Arrays.asList(row1, row2);

      Integer torrentMsgID = (Integer) redis.get(Store.TORRENT_INFO_MESSAGE_ID_KEY);
      if (Objects.isNull(torrentMsgID)) {
        Message msg = receiver.sendMsg(gid, "md", sb.toString(), columns);
        redis.set(Store.TORRENT_INFO_MESSAGE_ID_KEY, msg.getMessageId(), Store.TTL);
      } else {
        receiver.sendEditMsg(gid, torrentMsgID, "md", sb.toString(), columns);
        redis.expired(Store.TORRENT_INFO_MESSAGE_ID_KEY, Store.TTL);
      }
    } catch (HttpException e) { }
  }

  private void prevNext(Long gid, int page) {
    Integer torrentInfoMsgID = (Integer) redis.get(Store.TORRENT_INFO_MESSAGE_ID_KEY);
    if (Objects.nonNull(torrentInfoMsgID)) {
      receiver.sendDel(gid, torrentInfoMsgID);
      redis.del(Store.TORRENT_INFO_MESSAGE_ID_KEY);
    }

    Store.context.getBean(SearchCommand.class).sendSearch(gid, page);
  }

  private String cacheData(String source, String tid) {
    String uuid = UUID.randomUUID().toString();

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", source);
    if (Objects.nonNull(tid)) map.put("tid", tid);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

}
