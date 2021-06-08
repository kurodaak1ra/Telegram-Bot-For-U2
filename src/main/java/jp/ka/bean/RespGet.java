package jp.ka.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespGet {

  private Integer code;
  private byte[] data;
  private Document html;

}
