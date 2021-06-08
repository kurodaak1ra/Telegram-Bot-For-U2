package jp.ka;

import jp.ka.exception.HttpException;
import jp.ka.utils.HttpUtils;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.RespGet;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
public class AppTest {

  @Autowired
  private RedisUtils redis;

  @Test
  void testSplit() {
    try {
      RespGet resp = HttpUtils.get(495528934L, "/showup.php");
      Element table = resp.getHtml().getElementsByTag("table").get(10);
      Elements td = table.getElementsByTag("td");
      String uc = td.get(1).getElementsByTag("b").get(3).text();
      String res = td.get(2).getElementsByTag("fieldset").get(0).getElementsByTag("span").get(0).text().replaceAll("\\(", "").replaceAll("\\)", "").substring(0, 4);

    } catch (HttpException e) {
      e.printStackTrace();
    }
  }

}
