package jp.ka.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespPost {

  private Integer code;
  private Map<String, String> data;
  private Document html;

}
