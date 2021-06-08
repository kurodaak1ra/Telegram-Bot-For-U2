package jp.ka.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Redis implements Serializable {

  private static final long serialVersionUID = 2709425275741743919L;

  // 存入的对象
  private Object value;
  // 对象存入时 / 修改时的时间戳
  private long modifyTime;
  // 过期时间 单位 int:秒
  private int expireTime;

}
