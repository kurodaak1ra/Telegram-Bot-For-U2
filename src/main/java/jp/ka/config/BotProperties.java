package jp.ka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Kurenai
 * @since 2021-06-24 17:07
 */

@Data
@ConfigurationProperties(prefix = "bot")
public class BotProperties {

    private String api;
    private String token;
    private String username;

}
