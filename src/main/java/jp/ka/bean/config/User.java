package jp.ka.bean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "user")
public class User {

  private Long uid;

}