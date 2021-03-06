package jp.ka.utils;

import jp.ka.push.FreePush;
import jp.ka.push.PmPush;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommonUtils {

  public static String torrentStatus(String str, String promotionUpload, String promotionDownload) {
    if (str.equals("Promotion")) return String.format("ā%sā%s", promotionUpload.replaceAll("X", ""), promotionDownload.replaceAll("X", ""));
    return str;
    // switch (str) {
    //   case "FREE": return "šŗ1š»0";
    //   case "2X": return "šŗ2š»1";
    //   case "2X Free": return "šŗ2š»0";
    //   case "50%": return "šŗ0.5š»1";
    //   case "2X 50%": return "šŗ2š»0.5";
    //   case "30%": return "šŗ0.3š»1";
    //   case "Promotion": return String.format("šŗ%sš»%s", promotionUpload.replaceAll("X", ""), promotionDownload.replaceAll("X", ""));
    //   default: return "šŗ1š»1";
    // }
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
          String value = item.size() == 2 ? item.get(1) : "*";
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

  public static void pushServiceStart() {
    FreePush.start();
    PmPush.start();
  }

}
