package jp.ka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.util.Arrays;

@Configuration
public class BotConfig {

  @Bean
  public DefaultBotOptions defaultBotOptions() {
    DefaultBotOptions options = new DefaultBotOptions();
    options.setAllowedUpdates(Arrays.asList("message", "chat_member", "callback_query"));
    return options;
  }

}
