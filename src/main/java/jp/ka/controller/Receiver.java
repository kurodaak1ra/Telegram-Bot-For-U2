package jp.ka.controller;

import jp.ka.config.Config;
import jp.ka.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class Receiver extends TelegramLongPollingBot {

  @Value("${bot.token}")
  private String token;
  @Value("${bot.username}")
  private String username;

  private final CommandResolver commandResolver;
  private final CallbackResolver callbackResolver;

  public Receiver(CommandResolver commandResolver, CallbackResolver callbackResolver, DefaultBotOptions options) {
    super(options);
    this.commandResolver = commandResolver;
    this.callbackResolver = callbackResolver;
  }

  @Override
  public String getBotUsername() {
    return username;
  }

  @Override
  public String getBotToken() {
    return token;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && Objects.nonNull(update.getMessage().getNewChatMembers())) {
      for (User member : update.getMessage().getNewChatMembers()) {
        try {
          if (getMe().getId().equals(member.getId())) {
            leaveChat(update.getMessage().getChatId());
            return;
          }
        } catch (TelegramApiException e) {
          log.error("[onUpdateReceived -> getMe()]", e);
        }
      }
    }

    if (update.hasMessage()) {
      Message msg = update.getMessage();
      Long gid = msg.getChatId();
      Long uid = msg.getFrom().getId();
      if (!gid.equals(uid)) return;
      if (Objects.nonNull(Config.id) && !uid.equals(Config.id)) {
        sendMsg(gid, "md", "无权操作", null);
        return;
      }
      commandResolver.executeCommand(update);
    } else if (update.hasCallbackQuery()) {
      callbackResolver.executeCommand(update);
    }
  }

  public Message sendMsg(Long gid, String parse, String text, List<List<List<List<String>>>> columns) {
    SendMessage msg = new SendMessage(); // Create a SendMessage object with mandatory fields
    msg.setChatId(gid.toString());
    msg.setText(text);
    if (Objects.nonNull(columns)) msg.setReplyMarkup(CommonUtils.createMarkup(columns));
    if (!parse.equals("")) msg.setParseMode(parse.equals("md") ? "MarkdownV2" : "HTML");

    try {
      return execute(msg); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendMsg -> execute()]", e);
    }

    return null;
  }

  public void sendEditMsg(Long gid, Integer mid, String parse, String text, List<List<List<List<String>>>> columns) {
    EditMessageText message = new EditMessageText();
    message.setChatId(gid.toString());
    message.setMessageId(mid);
    message.setText(text);
    if (Objects.nonNull(columns))  message.setReplyMarkup(CommonUtils.createMarkup(columns));
    if (!parse.equals("")) message.setParseMode(parse.equals("md") ? "MarkdownV2" : "HTML");

    try {
      execute(message); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendEditMsg -> execute()]", e);
    }
  }

  public void sendEditMedia(Long gid, Integer mid, InputMedia file, List<List<List<List<String>>>> columns) {
    EditMessageMedia media = new EditMessageMedia();
    media.setChatId(gid.toString());
    media.setMessageId(mid);
    media.setMedia(file);
    if (Objects.nonNull(columns)) media.setReplyMarkup(CommonUtils.createMarkup(columns));

    try {
      execute(media); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendEditMedia -> execute()]", e);
    }
  }

  public Message sendDoc(Long gid, String parse, String caption, InputFile file, List<List<List<List<String>>>> columns) {
    SendDocument doc = new SendDocument();
    doc.setChatId(gid.toString());
    if (!parse.equals("")) doc.setParseMode(parse.equals("md") ? "MarkdownV2" : "HTML");
    if (Objects.nonNull(columns)) doc.setReplyMarkup(CommonUtils.createMarkup(columns));
    if (!caption.equals("")) doc.setCaption(caption);
    doc.setDocument(file);

    try {
      return execute(doc); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendDoc -> execute()]", e);
    }

    return null;
  }

  public Message sendImg(Long gid, String parse, String caption, InputFile img, List<List<List<List<String>>>> columns) {
    SendPhoto photo = new SendPhoto();
    photo.setChatId(gid.toString());
    if (!parse.equals("")) photo.setParseMode(parse.equals("md") ? "MarkdownV2" : "HTML");
    if (!caption.equals("")) photo.setCaption(caption);
    if (Objects.nonNull(columns)) photo.setReplyMarkup(CommonUtils.createMarkup(columns));
    photo.setPhoto(img);

    try {
      return execute(photo); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendImg -> execute()]", e);
    }

    return null;
  }

  public Boolean sendCallbackAnswer(String qid, boolean alert, String text) {
    AnswerCallbackQuery answer = new AnswerCallbackQuery();
    answer.setCallbackQueryId(qid);
    answer.setText(text); // 限制 200 字符
    answer.setShowAlert(alert);

    try {
      return execute(answer); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendCallbackAnswer -> execute()]", e);
    }

    return null;
  }

  public Boolean sendDel(Long gid, Integer mid) {
    DeleteMessage del = new DeleteMessage();
    del.setChatId(gid.toString());
    del.setMessageId(mid);

    try {
      return execute(del); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendDel -> execute()]", e);
    }

    return null;
  }

  public Boolean leaveChat(Long gid) {
    LeaveChat lc = new LeaveChat();
    lc.setChatId(gid.toString());

    try {
      return execute(lc);
    } catch (TelegramApiException e) {
      log.error("[leaveChat -> execute()]", e);
    }

    return null;
  }

}
