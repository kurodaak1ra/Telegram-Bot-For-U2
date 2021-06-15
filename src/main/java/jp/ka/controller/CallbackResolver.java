package jp.ka.controller;

import jp.ka.callback.Callback;
import jp.ka.config.Text;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class CallbackResolver {

  @Autowired
  private RedisUtils redis;

  private final Map<String, Callback> callbackMap = new HashMap<>();

  public void initCallbackMap(ApplicationContext context) {
    context.getBeansOfType(Callback.class).values().forEach(this::putCallback);
  }

  private void putCallback(Callback callback) {
    callbackMap.put(callback.cbk().name(), callback);
  }

  public void executeCommand(Update update) {
    CallbackQuery query = update.getCallbackQuery();

    String[] data = query.getData().split(":");
    Callback callback = callbackMap.get(data[0]);
    if (Objects.isNull(callback)) return;

    String qid = query.getId();
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();
    String uuid = "";
    if (data.length == 2) uuid = data[1];

    Map<String, Object> cache = null;
    boolean isUUID = Pattern.compile("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}").matcher(uuid).matches();
    if (isUUID) {
      cache = (Map<String, Object>) redis.get(data[1]);
      if (Objects.isNull(cache)) {
        Store.context.getBean(Receiver.class).sendDel(gid, mid);
        Store.context.getBean(Receiver.class).sendCallbackAnswer(qid, false, Text.CALLBACK_EXPIRE);
        return;
      }
      if (cache.get("source").equals("close")) Store.context.getBean(Receiver.class).sendDel(gid, mid);
    }

    callback.execute(query, cache);
  }

}
