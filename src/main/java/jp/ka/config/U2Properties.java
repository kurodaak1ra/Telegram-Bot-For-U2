package jp.ka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "u2")
@Data
public class U2Properties {

    private String domain = "https://u2.dmhy.org";
    private String cookie;

}
