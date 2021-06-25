package jp.ka.bean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;

@Data
@ConfigurationProperties(prefix = "bot.proxy")
public class BotProxy {

  private String    host;
  private int       port;
  private ProxyType type = ProxyType.NO_PROXY;

}
