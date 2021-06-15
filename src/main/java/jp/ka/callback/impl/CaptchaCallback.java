package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.command.impl.CaptchaCommand;
import jp.ka.controller.Receiver;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

@Component
public class CaptchaCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    receiver.sendDel(gid, mid);
    Store.context.getBean(CaptchaCommand.class).sendCaptcha(gid);
  }

  @Override
  public CBK cbk() {
    return CBK.CAPTCHA;
  }

}
