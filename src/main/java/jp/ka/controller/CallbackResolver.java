package jp.ka.controller;

import jp.ka.callback.Callback;
import jp.ka.variable.MsgTpl;
import jp.ka.utils.RedisUtils;
import jp.ka.variable.Store;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CallbackResolver {

  private final RedisUtils redis;

  private final Map<String, Callback> callbackMap = new HashMap<>();

  public CallbackResolver(RedisUtils redis) {
    this.redis = redis;
  }

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
    String cbData = "";
    if (data.length == 2) cbData = data[1];
    if (cbData.equals("close")) Store.context.getBean(Receiver.class).sendDel(gid, mid);

    Map<String, Object> cache = null;
    if (Pattern.compile("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}").matcher(cbData).matches()) {
      cache = (Map<String, Object>) redis.get(data[1]);
      if (Objects.isNull(cache)) {
        Store.context.getBean(Receiver.class).sendDel(gid, mid);
        Store.context.getBean(Receiver.class).sendCallbackAnswer(qid, false, MsgTpl.CALLBACK_EXPIRE);
        return;
      }
      if (cache.get("source").equals("close")) Store.context.getBean(Receiver.class).sendDel(gid, mid);
    }

    Store.context.getBean(Receiver.class).sendCallbackAnswer(qid, false, MsgTpl.CALLBACK_WAITING);
    callback.execute(query, cbData, cache);
  }

}
