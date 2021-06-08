package jp.ka.utils;

import jp.ka.bean.Redis;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

  // 默认的缓存容量
  private static int DEFAULT_CAPACITY = 512;
  // 最大容量
  private static int MAX_CAPACITY = 100000;
  // 刷新缓存的频率 s
  private static int MONITOR_DURATION = 60;
  // 使用默认容量创建一个 Map
  private static ConcurrentHashMap<String, Redis> cache = new ConcurrentHashMap<>(DEFAULT_CAPACITY);

  // 启动监控线程
  static {
    new Thread(new TimeoutTimerThread()).start();
  }

  /**
   * 将值通过序列化clone 处理后保存到缓存中，可以解决值引用的问题
   * @param key 键
   * @param value 值
   * @param expireTime 过期时间 s
   * @return
   */
  private boolean setCloneValue(String key, Object value, int expireTime) {
    try {
      if (cache.size() >= MAX_CAPACITY) {
        return false;
      }
      // 序列化赋值
      Redis entityClone = clone(new Redis(value, System.nanoTime(), expireTime));
      cache.put(key, entityClone);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 序列化 克隆处理
   * @param object
   * @return
   */
  private <T extends Serializable> T clone(T object) {
    T cloneObject = null;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(object);
      oos.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      cloneObject = (T) ois.readObject();
      ois.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cloneObject;
  }

  // expireTime -1 永不过期
  public boolean set(String key, Object value, int expireTime) {
    return setCloneValue(key, value, expireTime);
  }

  public Object get(String key) {
    Redis item = cache.get(key);
    if (Objects.isNull(item)) return null;
    if (isExpire(item)) cache.remove(key);
    return item.getValue();
  }

  public void clear() {
    cache.clear();
  }

  private static boolean isExpire(Redis item) {
    long timoutTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - item.getModifyTime());
    if (item.getExpireTime() > timoutTime) return false;
    return true;
  }

  static class TimeoutTimerThread implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          TimeUnit.SECONDS.sleep(MONITOR_DURATION);
          checkTime();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    // 过期缓存的具体处理方法
    private void checkTime() throws Exception {
      for (String key : cache.keySet()) {
        Redis item = cache.get(key);
        if (isExpire(item)) cache.remove(key); // 清除过期缓存和删除对应的缓存队列
      }
    }

  }

}
