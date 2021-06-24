package jp.ka.config;

import jp.ka.controller.CallbackResolver;
import jp.ka.controller.CommandResolver;
import jp.ka.controller.Receiver;
import jp.ka.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;

import java.util.Arrays;

/**
 * @author Kurenai
 * @since 2021-06-24 17:07
 */

@Configuration
@AutoConfigureAfter(MybatisAutoConfiguration.class)
@AutoConfigureBefore(TelegramBotStarterConfiguration.class)
@ConditionalOnProperty(prefix = "bot", name = "token")
@EnableConfigurationProperties({BotProperties.class, ProxyProperties.class, U2Properties.class})
public class BotAutoConfig {

    @Bean
    public Receiver receiver(CommandResolver commandResolver, CallbackResolver callbackResolver, BotProperties botProperties, DefaultBotOptions botOptions) {
        return new Receiver(commandResolver, callbackResolver, botProperties, botOptions);
    }

    @Bean
    public DefaultBotOptions defaultBotOptions(ProxyProperties proxyProperties, BotProperties botProperties) {
        DefaultBotOptions options = new DefaultBotOptions();
        if (!proxyProperties.getType().equals(ProxyType.NO_PROXY)) {
            options.setProxyHost(proxyProperties.getHost());
            options.setProxyPort(proxyProperties.getPort());
            options.setProxyType(proxyProperties.getType());
        }
        options.setAllowedUpdates(Arrays.asList("message", "chat_member", "callback_query"));
        if (StringUtils.isNotBlank(botProperties.getApi())) options.setBaseUrl(botProperties.getApi());
        return options;
    }

    @Bean
    public CallbackResolver callbackResolver(RedisUtils redis) {
        return new CallbackResolver(redis);
    }

    @Bean
    public CommandResolver commandResolver() {
        return new CommandResolver();
    }

    @Bean
    public BotInitializer botInitializer() {
        return new BotInitializer();
    }

}
