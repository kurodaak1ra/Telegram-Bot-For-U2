package jp.ka.controller;

import jp.ka.callback.Callback;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CallbackResolver {

  private final Map<String, Callback> callbackMap = new HashMap<>();

  public void initCallbackMap(ApplicationContext context) {
    context.getBeansOfType(Callback.class).values().forEach(this::putCallback);
  }

  private void putCallback(Callback callback) {
    callbackMap.put(callback.cbk().name(), callback);
  }

  public void executeCommand(Update update) {
    String data = update.getCallbackQuery().getData();
    Callback callback = callbackMap.get(data.split(":")[0]);
    if (Objects.isNull(callback)) return;

    callback.execute(update);
  }

}
