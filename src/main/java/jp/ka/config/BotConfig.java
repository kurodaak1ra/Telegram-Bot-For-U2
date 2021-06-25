package jp.ka.config;

import jp.ka.bean.config.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;

import java.util.Arrays;

@Configuration
public class BotConfig {

  @Bean
  public DefaultBotOptions defaultBotOptions(BotProxy proxy) {
    DefaultBotOptions options = new DefaultBotOptions();
    if (!proxy.getType().equals(ProxyType.NO_PROXY) && StringUtils.isNotBlank(proxy.getHost())) {
      options.setProxyHost(proxy.getHost());
      options.setProxyPort(proxy.getPort());
      options.setProxyType(proxy.getType());
    }
    options.setAllowedUpdates(Arrays.asList("message", "chat_member", "callback_query"));
    return options;
  }

}
