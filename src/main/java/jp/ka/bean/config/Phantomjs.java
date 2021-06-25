package jp.ka.bean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "phantomjs")
public class Phantomjs {

  private String path;

}
