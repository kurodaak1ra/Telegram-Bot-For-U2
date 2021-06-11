package jp.ka.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommonUtils {

  public static String torrentStatus(String str, String promotionUpload, String promotionDownload) {
    switch (str) {
      case "FREE": return "↑1↓0";
      case "2X": return "↑2↓1";
      case "2X Free": return "↑2↓0";
      case "50%": return "↑0.5↓1";
      case "2X 50%": return "↑2↓0.5";
      case "30%": return "↑0.3↓1";
      case "Promotion": return String.format("↑%s↓%s", promotionUpload.replaceAll("X", ""), promotionDownload.replaceAll("X", ""));
      default: return "↑1↓1";
    }
  }

  public static String formatMD(String msg) {
    msg = msg.replaceAll("\\|", "\\\\|");
    msg = msg.replaceAll("\\[", "\\\\[");
    msg = msg.replaceAll("\\]", "\\\\]");
    msg = msg.replaceAll("\\(", "\\\\(");
    msg = msg.replaceAll("\\)", "\\\\)");
    msg = msg.replaceAll("\\<", "\\\\<");
    msg = msg.replaceAll("\\>", "\\\\>");
    msg = msg.replaceAll("\\.", "\\\\.");
    msg = msg.replaceAll("\\-", "\\\\-");
    msg = msg.replaceAll("\\_", "\\\\_");
    msg = msg.replaceAll("\\#", "\\\\#");
    msg = msg.replaceAll("\\*", "\\\\*");
    msg = msg.replaceAll("\\`", "\\\\`");
    msg = msg.replaceAll("\\~", "\\\\~");
    msg = msg.replaceAll("\\+", "\\\\+");
    msg = msg.replaceAll("\\!", "\\\\!");
    msg = msg.replaceAll("\\=", "\\\\=");

    return msg;
  }

  public static InlineKeyboardMarkup createMarkup(List<List<List<List<String>>>> struct) {
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> column = new ArrayList<>();

    for (List<List<List<String>>> columns : struct) {
      for (List<List<String>> rows : columns) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (List<String> item : rows) {
          InlineKeyboardButton btn = new InlineKeyboardButton();
          btn.setText(item.get(0));
          String value = item.get(1);
          if (Pattern.compile("^http|^https").matcher(value).find()) btn.setUrl(value);
          else btn.setCallbackData(value);
          row.add(btn);
        }
        column.add(row);
      }
    }

    markup.setKeyboard(column);
    return markup;
  }

}
