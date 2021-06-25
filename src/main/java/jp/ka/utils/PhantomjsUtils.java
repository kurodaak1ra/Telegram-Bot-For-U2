package jp.ka.utils;

import jp.ka.bean.config.Phantomjs;
import jp.ka.bean.config.U2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PhantomjsUtils {

  private static U2 u2;
  private static Phantomjs phantomjs;

  @Autowired
  public void setU2(U2 u2) {
    this.u2 = u2;
  }
  @Autowired
  public void setPhantomjs(Phantomjs phantomjs) {
    this.phantomjs = phantomjs;
  }

  private static final String U2_COOKIE_KEY = "nexusphp_u2";

  public static PhantomJSDriver driver;

  public static void init() {
    driver = getPhantomJs();
  }

  private static PhantomJSDriver getPhantomJs() {
    DesiredCapabilities dc = new DesiredCapabilities();
    dc.setJavascriptEnabled(true);
    dc.setCapability("acceptSslCerts",true);
    dc.setCapability("takesScreenshot",true);
    dc.setCapability("cssSelectorsEnabled", true);
    dc.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", HttpUtils.UA);
    dc.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "User-Agent", HttpUtils.UA);
    dc.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjs.getPath());

    PhantomJSDriver phantomJSDriver = new PhantomJSDriver(dc);
    phantomJSDriver.manage().window().maximize(); // 窗口最大化
    phantomJSDriver.manage().window().setSize(new Dimension(1050, 1050));
    phantomJSDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS); // 设置等待时间  等待页面加载完成

    injectCookie(phantomJSDriver);

    return phantomJSDriver;
  }

  private static void injectCookie(PhantomJSDriver driver) {
    boolean login = false;
    Set<Cookie> cookies = driver.manage().getCookies();
    for (Cookie ck : cookies) {
      if (ck.getName().equals(U2_COOKIE_KEY)) login = true;
    }
    if (!login) {
      driver.get(u2.getDomain() + "/portal.php");

      Calendar c = Calendar.getInstance();
      c.setTime(new Date());
      c.add(Calendar.YEAR, 1);
      HashSet<Cookie> set = new HashSet<>();
      set.add(new Cookie(
        U2_COOKIE_KEY,
        HttpUtils.session.get(U2_COOKIE_KEY),
        u2.getDomain().split("//")[1],
        "/",
        c.getTime()
      ));
      setCookies(driver, set);
    }
  }

  private static void setCookies(PhantomJSDriver driver, Set<Cookie> cookies){
    if (Objects.isNull(cookies)) return;
    // Phantomjs 存在 Cookie 设置 bug, 只能通过 js 来设置了。
    StringBuilder sb = new StringBuilder();
    for (Cookie ck : cookies) {
      sb.append(String.format("document.cookie=\"%s=%s;path=%s;domain=%s\"", ck.getName(), ck.getValue(), ck.getPath(), ck.getDomain()));
    }
    driver.executeScript(sb.toString());
    log.info("[Phantomjs Inject Cookie] {}", driver.manage().getCookies());
  }

  @SneakyThrows
  public static List<File> captureEl(String url, String css) {
    List<File> screens = new ArrayList<>();

    driver.get(url);
    List<WebElement> webElements = driver.findElements(By.cssSelector(css));
    if (Objects.isNull(webElements)) return screens;

    for (WebElement element : webElements) {
      WrapsDriver wrapsDriver = (WrapsDriver) element;
      File screen = ((RemoteWebDriver) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE); // 截图整个页面
      int width = element.getSize().getWidth();
      int height = element.getSize().getHeight();
      java.awt.Rectangle rect = new java.awt.Rectangle(width, height); // 创建一个矩形使用上面的高度，和宽度
      Point p = element.getLocation(); // 得到元素的坐标

      BufferedImage img = ImageIO.read(screen);
      BufferedImage dest = img.getSubimage(p.getX() + 1, p.getY(), rect.width, rect.height); // 获得元素的高度和宽度
      ImageIO.write(dest, "png", screen); // 存为png格式
      screens.add(screen);
    }

    return screens;
  }

}
