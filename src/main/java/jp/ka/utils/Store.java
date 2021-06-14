package jp.ka.utils;

import org.springframework.context.ApplicationContext;

public class Store {

  public static ApplicationContext context;

  public static final int TTL = 5 * 30;

  public static final String ADV = "来自 Telegram Bot - U2 Tool Box\n\nDesigned by KA";

  public static final String SIGN_MESSAGE_ID_KEY = "sign.message.id";
  public static final String SIGN_MESSAGE_MARK_KEY = "sign.message.mark";

  public static final String TRANSFER_DATA_KEY = "transfer.data";
  public static final String TRANSFER_MARK_KEY = "transfer.mark";

  public static final int SEARCH_RESULT_COUNT = 14;
  public static final String SEARCH_DATA_KEY = "search.data";
  public static final String SEARCH_MARK_KEY = "search.mark";
  public static final String SEARCH_MESSAGE_ID_KEY = "search.message.id";
  public static final String SEARCH_OPTIONS_KEY = "search.page";

  public static final String TORRENT_INFO_MESSAGE_ID_KEY = "torrent.info.message.id";
  // public static final String TORRENT_LINK_MESSAGE_ID_KEY = "torrent.link.message.id";

}
