package jp.ka.config;

import jp.ka.bean.config.*;
import jp.ka.controller.CallbackResolver;
import jp.ka.controller.CommandResolver;
import jp.ka.controller.Receiver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;

import java.util.Arrays;

@Configuration
@EnableConfigurationProperties({Bot.class, BotProxy.class, User.class, U2.class, Phantomjs.class})
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

  // @Bean
  // public Receiver receiver(CommandResolver commandResolver, CallbackResolver callbackResolver, Bot bot, User user, DefaultBotOptions options) {
  //   return new Receiver(commandResolver, callbackResolver, bot, user, options);
  // }
  //
  // @Bean
  // public CallbackResolver callbackResolver() {
  //   return new CallbackResolver();
  // }
  //
  // @Bean
  // public CommandResolver commandResolver(User user) {
  //   return new CommandResolver(user);
  // }

}
