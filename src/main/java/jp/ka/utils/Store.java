package jp.ka.utils;

import jp.ka.command.Command;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store {

  public static ApplicationContext context;

  public static Command.CMD STEP;

  public static final int TTL = 5 * 30;

  public static final String ADV = "来自 Telegram Bot - U2Info Tool Box\n\nDesigned by KA";

  public static int CAPTCHA_MESSAGE_ID = -1;

  public static int SIGN_MESSAGE_ID = -1;
  public static String SIGN_MESSAGE_MARK = "";

  public static boolean TRANSFER_MARK = false;
  public static List<Integer> TRANSFER_LIST = new ArrayList<>();

  public static String SEARCH_MARK = "";
  public static int SEARCH_MESSAGE_ID = -1;
  public static final int SEARCH_RESULT_COUNT = 10;
  public static final String SEARCH_DATA_KEY = "search.data";
  public static final String SEARCH_OPTIONS_KEY = "search.options";

  public static int TORRENT_INFO_MESSAGE_ID = -1;

  public static String TORRENT_MAGIC_TID = "";
  public static String TORRENT_MAGIC_FOR = "";
  public static String TORRENT_MAGIC_HOURS = "";
  public static String TORRENT_MAGIC_TYPE = "";

  public static Map<String, Map<String, String>> FREE_MARK = new HashMap<>();

  public static boolean PM_PUSH = true;
  public static boolean FREE_PUSH = true;

}
