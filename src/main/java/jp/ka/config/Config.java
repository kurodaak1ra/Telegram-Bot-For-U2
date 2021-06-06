package jp.ka.config;

import jp.ka.command.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class Config {

  public static Long uid;
  public static String cookieKey;
  @Value("${u2.cookie_key}")
  public void setCookieKey(String key) {
    this.cookieKey = key;
  }

  public static Command.CMD step;
  public static Map<String, String> session = new HashMap<>();

}
