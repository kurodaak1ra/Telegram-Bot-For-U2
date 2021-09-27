package jp.ka.callback.impl;

import jp.ka.callback.Callback;
import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.push.FreePush;
import jp.ka.push.PmPush;
import jp.ka.push.SignPush;
import jp.ka.variable.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Arrays;
import java.util.Map;

@Component
public class PushServiceCallback implements Callback {

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(CallbackQuery query, String cbData, Map<String, Object> cache) {
    Long gid = query.getMessage().getChatId();
    Integer mid = query.getMessage().getMessageId();

    switch (cbData) {
      case "pm": {
        if (Store.PM_PUSH) PmPush.stop();
        else PmPush.start();
        break;
      }
      case "free": {
        if (Store.FREE_PUSH) FreePush.stop();
        else FreePush.start();
        break;
      }
        case "sign": {
            if (Store.SIGN_PUSH) SignPush.stop();
            else SignPush.start();
            break;
        }
    }

    receiver.sendEditMsg(gid, mid, "md", "*推送服务控制台*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("PM 提醒"),
        Arrays.asList(Store.PM_PUSH ? "■" : "□", Command.CMD.PUSH_SERVICE + ":pm")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("全站 FREE 提醒"),
        Arrays.asList(Store.FREE_PUSH ? "■" : "□", Command.CMD.PUSH_SERVICE + ":free")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("签到 提醒(12:00)"),
        Arrays.asList(Store.SIGN_PUSH ? "■" : "□", Command.CMD.PUSH_SERVICE + ":sign")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("❌", Callback.CBK.TORRENT_LINK + ":close")
      ))
    ));
  }

  @Override
  public CBK cbk() {
    return CBK.PUSH_SERVICE;
  }

}
