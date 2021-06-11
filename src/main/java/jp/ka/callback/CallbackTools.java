package jp.ka.callback;

import jp.ka.config.Text;
import jp.ka.controller.Receiver;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;
import java.util.Objects;

public class CallbackTools {

  private static final RedisUtils redis = Store.context.getBean(RedisUtils.class);
  private static final Receiver receiver = Store.context.getBean(Receiver.class);

  public static Map<String, Object> hasExpired(Long gid, CallbackQuery query) {
    Map<String, Object> cache = (Map<String, Object>) redis.get(query.getData().split(":")[1]);
    if (Objects.isNull(cache)) {
      receiver.sendDel(gid, query.getMessage().getMessageId());
      receiver.sendCallbackAnswer(query.getId(), false, Text.CALLBACK_EXPIRE);
      return null;
    }

    return cache;
  }

}
