package jp.ka.bean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bot")
public class Bot {

  private String token;
  private String username;

}
