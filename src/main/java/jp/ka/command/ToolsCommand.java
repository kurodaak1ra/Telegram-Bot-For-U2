package jp.ka.command;

import jp.ka.config.U2;
import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RespGet;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;

public class ToolsCommand {

  public static boolean setUserData(Long gid) {
    try {
      RespGet resp = HttpUtils.get(gid,"/index.php");
      Elements mediums = resp.getHtml().getElementsByClass("medium");
      if (mediums.size() == 0) return false;
      Element medium = mediums.get(0);
      // =======
      Elements href = medium.getElementsByAttribute("href");
      Element user = href.get(0);
      U2.uid = user.attr("href").split("=")[1];
      U2.username = user.getElementsByAttribute("dir").get(0).text();
      U2.pageKey = href.get(1).attr("href").split("=")[1];
      // =======
      List<TextNode> text = medium.textNodes();
      U2.shareRate = text.get(7).toString().trim();
      U2.uploads = text.get(8).toString().trim();
      U2.downloads = text.get(9).toString().trim();
      U2.invite = text.get(12).splitText(1).toString().trim();
      U2.client = text.get(13).toString().split(" ")[1].trim();
      U2.uploading = text.get(14).toString().trim();
      U2.downloading = text.get(15).toString().split("\\)")[0].trim();
      // =====
      Element uCoin = medium.getElementsByClass("ucoin-notation").get(0);
      U2.coinGold = uCoin.getElementsByClass("ucoin-gold").text();
      U2.coinSilver = uCoin.getElementsByClass("ucoin-silver").text();
      U2.coinCopper = uCoin.getElementsByClass("ucoin-copper").text();
      return true;
    } catch (HttpException e) {
      return false;
    }
  }

}
