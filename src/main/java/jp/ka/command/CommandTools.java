package jp.ka.command;

import jp.ka.bean.U2Info;
import jp.ka.config.Config;
import jp.ka.config.U2;
import jp.ka.controller.Receiver;
import jp.ka.mapper.U2Mapper;
import jp.ka.utils.CommonUtils;
import jp.ka.utils.HttpUtils;
import jp.ka.bean.RespGet;
import jp.ka.utils.Store;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;

public class CommandTools {

  private static final Receiver receiver = Store.context.getBean(Receiver.class);

  private static final U2Mapper mapper = Store.context.getBean(U2Mapper.class);

  public static void userInfo(Long gid) {
    RespGet resp = HttpUtils.get(gid,"/index.php");
    Element medium = resp.getHtml().getElementsByClass("medium").get(0);
    // ========================================================
    String username = medium.children().get(0).child(0).text();
    // ========================================================
    List<TextNode> text = medium.textNodes();
    String shareRate = text.get(7).toString().trim();
    String uploads = text.get(8).toString().trim();
    String downloads = text.get(9).toString().trim();
    String invite = text.get(12).splitText(1).toString().trim();
    String client = "";
    String uploading = "";
    String downloading = "";
    if (text.size() == 16) {
      client = text.get(13).toString().split(" ")[1].trim();
      uploading = text.get(14).toString().trim();
      downloading = text.get(15).toString().split("")[0].trim();
    } else {
      Elements a = medium.getElementsByTag("a");
      client = a.get(9).text();
      Element a10 = a.get(10);
      String aLink = a10.attr("href").split("#")[1];
      if (aLink.equals("seedlist")) {
        uploading = a10.text();
        downloading = "0";
      } else if (aLink.equals("leechlist")) {
        uploading = "0";
        downloading = a10.text();
      }
      if (a.size() == 12) downloading = a.get(11).text();
    }
    // ========================================================
    Element uCoin = medium.getElementsByClass("ucoin-notation").get(0);
    String coinGold = uCoin.getElementsByClass("ucoin-gold").text();
    String coinSilver = uCoin.getElementsByClass("ucoin-silver").text();
    String coinCopper = uCoin.getElementsByClass("ucoin-copper").text();
    // ========================================================
    String tmpMsg = String.format("*\\[个人信息\\]*\n\nUID: `%s`\n用户名: `%s`\n分享率: `%s`\n上传量: `%s`\n下载量: `%s`\nUCoin: `%s%s%s`\n邀请: `%s`\n客户端: `%s`\n上传: `%s`\n下载: `%s`",
        U2.uid, CommonUtils.formatMD(username), shareRate, uploads, downloads, coinGold.equals("") ? "" : "\uD83E\uDD47" + coinGold, coinSilver.equals("") ? "" : "\uD83E\uDD48" + coinSilver, coinCopper.equals("") ? "" : "\uD83E\uDD49" + coinCopper, invite, client, uploading, downloading);
    receiver.sendMsg(gid, "md", tmpMsg, null);
  }

  public static void setData(Long id) {
    RespGet resp1 = HttpUtils.get(Config.id, "/usercp.php");
    U2.passKey = resp1.getHtml().getElementsByClass("hidden-click").get(0).attr("data-content");

    RespGet resp2 = HttpUtils.get(Config.id,"/index.php");
    Elements c = resp2.getHtml().getElementsByClass("medium").get(0).children();
    U2.uid = c.get(0).child(0).attr("href").split("id=")[1];
    U2.pageKey = c.get(1).attr("href").split("key=")[1];

    mapper.insertInfo(new U2Info(id, Integer.valueOf(U2.uid), U2.pageKey, U2.passKey));
  }

}
