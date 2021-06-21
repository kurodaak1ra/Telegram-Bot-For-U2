package jp.ka.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class U2Info {

  private Long id;

  private Integer uid;
  private String pageKey;
  private String passKey;

}
