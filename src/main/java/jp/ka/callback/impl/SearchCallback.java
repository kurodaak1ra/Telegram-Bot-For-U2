package jp.ka.callback.impl;

import jp.ka.bean.RespGet;
import jp.ka.callback.Callback;
import jp.ka.command.impl.SearchCommand;
import jp.ka.config.Config;
import jp.ka.variable.MsgTpl;
import jp.ka.controller.Receiver;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.variable.Store;
import lombok.SneakyThrows;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;

@Component
public class SearchCallback implements Callback {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();

    List<Map<String, String>> items = (List<Map<String, String>>) redis.get(Store.SEARCH_DATA_KEY);
    Map<String, Object> options = (Map<String, Object>) redis.get(Store.SEARCH_OPTIONS_KEY);
    String mark = (String) cache.get("mark");
    if (!mark.equals(Store.SEARCH_MARK) || Objects.isNull(items) || Objects.isNull(options)) {
      receiver.sendDel(gid, query.getMessage().getMessageId());
      receiver.sendCallbackAnswer(query.getId(), true, MsgTpl.CALLBACK_EXPIRE);
      return;
    }

    redis.expired(Store.SEARCH_DATA_KEY, Store.TTL);
    redis.expired(Store.SEARCH_OPTIONS_KEY, Store.TTL);

    String source = (String) cache.get("source");
    Integer offset = (Integer) cache.get("offset");
    switch (source) {
      case "item": {
        Integer index = (Integer) cache.get("index");
        item(gid, items.get(index));
        break;
      }
      case "prev": {
        prevNext(gid, offset);
        break;
      }
      case "next": {
        int pageSize = items.size() / Store.SEARCH_RESULT_COUNT + (items.size() % Store.SEARCH_RESULT_COUNT == 0 ? 0 : 1);
        if (offset + 1 == pageSize) {
          int webPageSize = (int) options.get("page_size");
          int webOffset = (int) options.get("offset") + 1;
          if (webOffset < webPageSize) {
            options.put("offset", webOffset);
            redis.set(Store.SEARCH_OPTIONS_KEY, options, Store.TTL);
            Store.context.getBean(SearchCommand.class).initData(gid);
          }
        }
        prevNext(gid, offset);
        break;
      }
    }
  }

  @Override
  public CBK cbk() {
    return CBK.SEARCH;
  }

  @SneakyThrows
  private void item(Long gid, Map<String, String> item) {
    RespGet resp = HttpUtils.get(gid, "/details.php?id=" + item.get("tid") + "&hit=1");
    Elements trs = resp.getHtml().getElementById("outer").getElementsByTag("table").get(0).getElementsByTag("tr");

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("[%s](%s/details.php?id=%s&hit=1)\n", CommonUtils.formatMD(item.get("name")), Config.U2Domain, item.get("tid")));
    for (int i = 0; i < trs.size(); i++) {
      Element tr = trs.get(i);
      String title = trs.get(i).getElementsByTag("td").get(0).text();
      switch (title) {
        // case "下载": {
        //   Element download = tr.getElementsByTag("a").get(1);
        //   U2.passKey = download.attr("href").split("&")[1].split("=")[1];
        //   break;
        // }
        case "副标题": {
          Element td = tr.getElementsByTag("td").get(1);
          sb.append("_" + CommonUtils.formatMD(td.text()) + "_\n");
          break;
        }
        case "基本信息": {
          List<TextNode> tn = tr.getElementsByTag("td").get(1).textNodes();
          sb.append("\n*TID*: `" + item.get("tid") + "`");
          sb.append("\n*大小*: `" + tn.get(2).text().trim() + "`");
          sb.append("\n*类型*: `" + tn.get(3).text().trim() + "`");
          Element release = tr.getElementsByTag("time").get(0);
          sb.append("\n*发布时间*: `" + release.text() + "`");
          // System.out.println(release.attr("title"));
          break;
        }
        case "流量优惠": {
          Elements service = tr.getElementsByTag("time");
          if (service.size() != 0) {
            sb.append("\n*优惠类型*: `" + CommonUtils.torrentStatus(item.get("status"), item.get("status_promotion_upload"), item.get("status_promotion_download")) + "`");
            sb.append("\n*优惠剩余*: `" + service.get(0).text() + "`");
            // System.out.println(service.attr("title"));
          } else {
            Elements icon = tr.getElementsByTag("img");
            if (icon.size() != 0) {
              sb.append("\n*优惠类型*: `"+ icon.get(0).attr("alt") +"`");
            } else {
              sb.append("\n*优惠类型*: `普通`");
            }
          }
          break;
        }
        case "活力度": {
          Elements b = tr.getElementsByTag("b");
          if (b.size() == 1) continue;
          String averageProcess = tr.getElementsByTag("td").textNodes().get(1).text().replaceAll("\\(", "").replaceAll("\\)", "").trim();
          sb.append("\n*平均进度*: `" + averageProcess + "`");
          sb.append("\n*平均速度*: `" + b.get(1).text() + "`");
          sb.append("\n*总速度*: `" + b.get(2).text() + "`");
          break;
        }
      }
    }

    List<List<List<List<String>>>> columns = Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("🔗", CBK.TORRENT_INFO + ":" + cacheData("torrent_link", item.get("tid"))),
        Arrays.asList("施放魔法", CBK.TORRENT_INFO + ":" + cacheData("magic", item.get("tid")))
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("❌", CBK.TORRENT_INFO + ":" + cacheData("close", null))
      ))
    );

    if (Store.TORRENT_INFO_MESSAGE_ID == -1) {
      Message msg = receiver.sendMsg(gid, "md", sb.toString(), columns);
      Store.TORRENT_INFO_MESSAGE_ID = msg.getMessageId();
    } else {
      receiver.sendEditMsg(gid, Store.TORRENT_INFO_MESSAGE_ID, "md", sb.toString(), columns);
    }
  }

  private void prevNext(Long gid, int page) {
    if (Store.TORRENT_INFO_MESSAGE_ID != -1) {
      receiver.sendDel(gid, Store.TORRENT_INFO_MESSAGE_ID);
      Store.TORRENT_INFO_MESSAGE_ID = -1;
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
