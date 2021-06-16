package jp.ka.command.impl;

import jp.ka.bean.RespGet;
import jp.ka.command.Command;
import jp.ka.config.Config;
import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.exception.HttpException;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Component
public class SearchCommand implements Command {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    String[] split = msg.getText().split("\n");
    if (split.length > 2) {
      prompt(gid);
      return;
    }

    String keywords = "";
    if (split.length > 1) keywords = split[1].trim();
    HashMap<String, Object> map = new HashMap<>();
    map.put("keywords", keywords);
    map.put("offset", 0);
    Store.SEARCH_MARK = UUID.randomUUID().toString();
    redis.set(Store.SEARCH_OPTIONS_KEY, map, Store.TTL);
    redis.set(Store.SEARCH_DATA_KEY, new ArrayList<Map<String, String>>(), Store.TTL);

    if (Store.SEARCH_MESSAGE_ID != -1) {
      receiver.sendDel(gid, Store.SEARCH_MESSAGE_ID);
      Store.SEARCH_MESSAGE_ID = -1;
    }
    if (Store.TORRENT_INFO_MESSAGE_ID != -1) {
      receiver.sendDel(gid, Store.TORRENT_INFO_MESSAGE_ID);
      Store.TORRENT_INFO_MESSAGE_ID = -1;
    }

    boolean flag = initData(gid);
    if (flag) sendSearch(gid, 0);
  }

  @Override
  public CMD cmd() {
    return CMD.SEARCH;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "搜索";
  }

  @Override
  public Message prompt(Long gid) {
    return receiver.sendMsg(gid, "md", Text.COMMAND_ERROR + "\n\n`/search`\n`<keywords - 关键词 (可省略)>`", null);
  }

  private String formatName(String msg) {
    msg = CommonUtils.formatMD(msg);

    int limit = 70; // 查询结果显示字符数
    if (msg.length() <= limit) return msg;
    else return msg.substring(0, limit) + " \\.\\.\\.";
  }

  private String formatSize(String msg) {
    msg = msg.replaceAll("iB", "");
    String tmpUnit = msg.substring(msg.length() - 1);
    msg = msg.split("\\.")[0];

    return msg + tmpUnit;
  }

  public boolean initData(Long gid) {
    Map<String, Object> options = (Map<String, Object>) redis.get(Store.SEARCH_OPTIONS_KEY);
    if (Objects.isNull(options)) return false;

    try {
      String keywords = null;
      try {
        keywords = URLEncoder.encode((String) options.get("keywords"), "UTF-8");
      } catch (UnsupportedEncodingException e) { }
      RespGet resp = HttpUtils.get(gid, "/torrents.php?page="+ options.get("offset") +"&search=" + keywords);
      Elements torrents = resp.getHtml().getElementsByClass("torrents");
      if (torrents.size() == 0) {
        receiver.sendMsg(gid, "md", "*没有种子！ 请用准确的关键字重试*", null);
        return false;
      }

      Element pagination = resp.getHtml().getElementById("outer").getElementsByClass("embedded").get(0).children().get(1);
      Elements linkBtn = pagination.getElementsByTag("a");
      int pageSize = 1;
      if (linkBtn.size() != 0) {
        Element first = pagination.child(3).getElementsByTag("b").get(0);
        Integer limit = Integer.valueOf(first.text().split("-")[1].trim());

        Element last = linkBtn.get(linkBtn.size() - 1).getElementsByTag("b").get(0);
        Integer max = Integer.valueOf(last.text().split("-")[1].trim());

        pageSize = (max / limit) + (max % limit == 0 ? 0 : 1);
      }
      options.put("page_size", pageSize);
      redis.set(Store.SEARCH_OPTIONS_KEY, options, Store.TTL);

      Elements list = torrents.get(0).getElementsByTag("tr");
      List<Map<String, String>> items = new ArrayList<>();
      for (int i = 1; i < list.size(); i+=3) {
        Element target = list.get(i);
        Element type = target.getElementsByTag("a").get(0);
        Element name = target.getElementsByClass("tooltip").get(0);

        Element status = new Element("html");
        Elements statusImg = target.getElementsByTag("table").get(0).getElementsByTag("tr").get(1).getElementsByTag("img");
        if (statusImg.size() > 0) status = statusImg.get(0);
        Element statusPromotionUpload = null;
        Element statusPromotionDownload = null;
        if (status.attr("alt").equals("Promotion")) {
          Elements statusPromotion = target.getElementsByTag("table").get(0).getElementsByTag("b");
          if (statusPromotion.size() == 3) {
            statusPromotionUpload = statusPromotion.get(0);
            statusPromotionDownload = statusPromotion.get(1);
          } else {
            statusPromotionUpload = statusPromotion.get(1);
            statusPromotionDownload = statusPromotion.get(2);
          }
        }

        Element description = target.getElementsByClass("tooltip").get(1);
        Element torrent = target.getElementsByClass("embedded").get(0).getElementsByTag("a").get(0);
        Element uploadTime = target.getElementsByTag("time").get(0);
        Element size = target.getElementsByClass("rowfollow").get(4);

        Element seederTag = target.getElementsByClass("rowfollow").get(5).getAllElements().get(0);
        Element seeder = null;
        if (seederTag.getElementsByTag("a").size() != 0) seeder = seederTag.getElementsByTag("a").get(0);
        else if (seederTag.getElementsByTag("span").size() != 0) seeder = seederTag.getElementsByTag("span").get(0);
        else seeder = seederTag;

        Element downloaderTag = target.getElementsByClass("rowfollow").get(6).getAllElements().get(0);
        Element downloader = null;
        if (downloaderTag.getElementsByTag("a").size() != 0) downloader = downloaderTag.getElementsByTag("a").get(0);
        else if (downloaderTag.getElementsByTag("span").size() != 0) downloader = downloaderTag.getElementsByTag("span").get(0);
        else downloader = downloaderTag;

        // ========================================================

        HashMap<String, String> map = new HashMap<>();
        map.put("tid", name.attr("href").split("=")[1].split("&")[0]);
        map.put("type", type.text());
        map.put("name", name.text());
        map.put("status", status.attr("alt"));
        if (status.attr("alt").equals("Promotion")) {
          map.put("status_promotion_upload", statusPromotionUpload.text());
          map.put("status_promotion_download", statusPromotionDownload.text());
        }
        map.put("description", description.text());
        map.put("torrent", torrent.attr("href"));
        map.put("uploadTime", uploadTime.attr("title"));
        map.put("size", size.text());
        map.put("seeder", seeder.text());
        map.put("downloader", downloader.text());

        items.add(map);
      }

      List<Map<String, String>> cacheItems = (List<Map<String, String>>) redis.get(Store.SEARCH_DATA_KEY);
      if (Objects.isNull(cacheItems)) redis.set(Store.SEARCH_DATA_KEY, items, Store.TTL);
      else {
        cacheItems.addAll(items);
        redis.set(Store.SEARCH_DATA_KEY, cacheItems, Store.TTL);
      }
      return true;
    } catch (HttpException e) { }
    return false;
  }

  public void sendSearch(Long gid, int page) {
    List<Map<String, String>> items = (List<Map<String, String>>) redis.get(Store.SEARCH_DATA_KEY);
    if (Objects.isNull(items)) return;

    StringBuilder sb = new StringBuilder();
    List<List<List<List<String>>>> columns = new ArrayList<>();
    List<List<String>> rows = new ArrayList<>();
    for (int i = page * Store.SEARCH_RESULT_COUNT; i < items.size(); i++) {
      if (i == (page + 1) * Store.SEARCH_RESULT_COUNT) break;
      String index = i + 1 < 10 ? "0" + (i + 1) : "" + (i + 1);

      Map<String, String> item = items.get(i);
      sb.append(String.format("*%s*\\. `\uD83D\uDCBE%s\\|%s\\|\uD83C\uDE39%s`\n[%s](%s)\n",
          index,
          formatSize(item.get("size")),
          "\uD83D\uDC46" + item.get("seeder") + "\uD83D\uDC47" + item.get("downloader"),
          CommonUtils.torrentStatus(item.get("status"), item.get("status_promotion_upload"), item.get("status_promotion_download")),
          formatName(item.get("name")),
          Config.U2Domain + "/details.php?id=" + item.get("tid") + "&hit=1")
      );

      String uuid = cacheData("item", Store.SEARCH_MARK, null, i);
      List<String> row = Arrays.asList(index, CMD.SEARCH + ":" + uuid);
      rows.add(row);

      if ((i + 1) % (Store.SEARCH_RESULT_COUNT / 2) == 0) {
        columns.add(Arrays.asList(rows));
        rows = new ArrayList<>(); // 不能 clear，丢到 columns 里的是指针
      }
    }
    if (rows.size() != 0) columns.add(Arrays.asList(rows));
    List<List<List<List<String>>>> tmpColumn = features(columns, Store.SEARCH_MARK, items.size(), page, Store.TTL);

    if (Store.SEARCH_MESSAGE_ID == -1) {
      Message msg = receiver.sendMsg(gid, "md", sb.toString(), tmpColumn);
      Store.SEARCH_MESSAGE_ID = msg.getMessageId();
    } else receiver.sendEditMsg(gid, Store.SEARCH_MESSAGE_ID, "md", sb.toString(), tmpColumn);
  }

  private List<List<List<List<String>>>> features(List<List<List<List<String>>>> columns, String mark, int total, int page, int ttl) {
    List<List<String>> option = new ArrayList<>();

    if (page != 0) {
      String uuid = cacheData("prev", mark, page - 1, null);
      option.add(Arrays.asList("⬅️ 上一页", CMD.SEARCH + ":" + uuid));
    }
    if ((page + 1) * Store.SEARCH_RESULT_COUNT < total) {
      String uuid = cacheData("next", mark, page + 1, null);
      option.add(Arrays.asList("下一页 ➡️", CMD.SEARCH + ":" + uuid));
    }

    columns.add(Arrays.asList(option));
    return columns;
  }

  private String cacheData(String source, String mark, Integer offset, Integer index) {
    String uuid = UUID.randomUUID().toString();

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", source);
    map.put("mark", mark);
    if (Objects.nonNull(offset)) map.put("offset", offset);
    if (Objects.nonNull(index)) map.put("index", index);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

}
