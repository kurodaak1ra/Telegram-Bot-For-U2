package jp.ka.bean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "u2")
public class U2 {

  private String domain = "https://u2.dmhy.org";
  private String cookie;

}
