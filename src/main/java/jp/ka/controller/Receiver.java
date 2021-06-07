package jp.ka.controller;

import jp.ka.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Component
public class Receiver extends TelegramLongPollingBot {

  private static String api;
  @Value("${bot.api}")
  public void setApi(String api) {
    this.api = api;
  }
  @Value("${bot.token}")
  private String token;
  @Value("${bot.username}")
  private String username;

  private final Resolver resolver;

  public Receiver(Resolver resolver) {
    super(options());
    this.resolver = resolver;
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

    Message msg = null;
    if (update.hasMessage()) msg = update.getMessage();
    else if (update.hasCallbackQuery()) msg = update.getCallbackQuery().getMessage();

    if (Objects.isNull(msg)) return;
    if (!update.getMessage().getChat().getId().equals(update.getMessage().getFrom().getId())) return;
    if (!msg.getFrom().getId().equals(Config.uid)) {
      sendMsg(msg.getChatId(), "无权操作", "md", -1);
      return;
    }

    resolver.executeCommand(update);
  }

  private static DefaultBotOptions options() {
    DefaultBotOptions opt = new DefaultBotOptions();
    opt.setAllowedUpdates(Arrays.asList("message", "chat_member", "callback_query"));
    if (Objects.nonNull(api)) opt.setBaseUrl(api);

    return opt;
  }

  public Message sendMsg(Long gid, String text, String parse, Integer reply) {
    SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
    message.setChatId(gid.toString());
    message.setText(text);
    if (!parse.equals("")) message.setParseMode(parse.equals("md") ? "MarkdownV2" : "HTML");
    if (reply.longValue() > 0) message.setReplyToMessageId(reply);

    try {
      return execute(message); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendMsg -> execute()]", e);
    }

    return null;
  }

  public Message sendDocs(Long gid, String caption, InputFile file) {
    SendDocument doc = new SendDocument();
    doc.setChatId(gid.toString());
    if (!caption.equals("")) doc.setCaption(caption);
    doc.setDocument(file);

    try {
      return execute(doc); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendDocs -> execute()]", e);
    }

    return null;
  }

  public Boolean delMsg(Long gid, Integer mid) {
    DeleteMessage del = new DeleteMessage();
    del.setChatId(gid.toString());
    del.setMessageId(mid);

    try {
      return execute(del); // Call method to send the message
    } catch (TelegramApiException e) {
      log.error("[sendDocs -> execute()]", e);
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
