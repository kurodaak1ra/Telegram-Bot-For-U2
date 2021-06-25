package jp.ka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;

/**
 * @author Kurenai
 * @since 2021-06-24 17:10
 */

@Data
@ConfigurationProperties(prefix = "bot.proxy")
public class ProxyProperties {

    private String    host;
    private int       port;
    private ProxyType type = ProxyType.NO_PROXY;

}
