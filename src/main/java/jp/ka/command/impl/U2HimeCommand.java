package jp.ka.command.impl;

import jp.ka.command.Command;
import jp.ka.controller.Receiver;
import jp.ka.utils.RedisUtils;
import jp.ka.utils.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

@Component
public class U2HimeCommand implements Command {

  @Autowired
  private RedisUtils redis;

  @Autowired
  private Receiver receiver;

  @Override
  public void execute(Message msg) {
    Long gid = msg.getChatId();

    receiver.sendMsg(gid, "md", "*调戏 U2 娘*", Arrays.asList(
      Arrays.asList(Arrays.asList(
        Arrays.asList("分享", CMD.U2_HIME + ":我的分享率"),
        Arrays.asList("实际", CMD.U2_HIME + ":我的实际分享率"),
        Arrays.asList("赚分", CMD.U2_HIME + ":我的赚分速度"),
        Arrays.asList("我币", CMD.U2_HIME + ":我的UCoin"),
        Arrays.asList("站币", CMD.U2_HIME + ":全站UC存量"),
        Arrays.asList("签名", CMD.U2_HIME + ":签名条"),
        Arrays.asList("取向", CMD.U2_HIME + ":我的取向"),
        Arrays.asList("后宫", CMD.U2_HIME + ":我的后宫")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("早安", CMD.U2_HIME + ":早安"),
        Arrays.asList("晚安", CMD.U2_HIME + ":晚安"),
        Arrays.asList("战力", CMD.U2_HIME + ":我的战斗力"),
        Arrays.asList("买起", CMD.U2_HIME + ":多少人买得起邀请"),
        Arrays.asList("比壕", CMD.U2_HIME + ":U2有多少人比我壕"),
        Arrays.asList("百合", CMD.U2_HIME + ":百合"),
        Arrays.asList("败犬", CMD.U2_HIME + ":败犬"),
        Arrays.asList("变身", CMD.U2_HIME + ":变身")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("安慰", CMD.U2_HIME + ":求安慰"),
        Arrays.asList("包养", CMD.U2_HIME + ":求包养"),
        Arrays.asList("合体", CMD.U2_HIME + ":求合体"),
        Arrays.asList("交往", CMD.U2_HIME + ":求交往"),
        Arrays.asList("交尾", CMD.U2_HIME + ":求交尾"),
        Arrays.asList("求虐", CMD.U2_HIME + ":求虐"),
        Arrays.asList("调教", CMD.U2_HIME + ":求调教"),
        Arrays.asList("中出", CMD.U2_HIME + ":求中出")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("出柜", CMD.U2_HIME + ":出柜"),
        Arrays.asList("出击", CMD.U2_HIME + ":出击"),
        Arrays.asList("答案", CMD.U2_HIME + ":the answer to life, the universe, and everything"),
        Arrays.asList("回家", CMD.U2_HIME + ":跟我回家去"),
        Arrays.asList("倒茶", CMD.U2_HIME + ":倒茶"),
        Arrays.asList("递归", CMD.U2_HIME + ":递归"),
        Arrays.asList("风儿", CMD.U2_HIME + ":今天的风儿好喧嚣啊"),
        Arrays.asList("教义", CMD.U2_HIME + ":教义")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("节操", CMD.U2_HIME + ":掉节操"),
        Arrays.asList("捐赠", CMD.U2_HIME + ":捐赠"),
        Arrays.asList("嚼酒", CMD.U2_HIME + ":我要口嚼酒"),
        Arrays.asList("姥爷", CMD.U2_HIME + ":姥爷"),
        Arrays.asList("萝莉", CMD.U2_HIME + ":萝莉"),
        Arrays.asList("麻将", CMD.U2_HIME + ":麻将"),
        Arrays.asList("买基", CMD.U2_HIME + ":买基"),
        Arrays.asList("卖萌", CMD.U2_HIME + ":卖个萌")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("喵帕", CMD.U2_HIME + ":喵帕斯"),
        Arrays.asList("藐视", CMD.U2_HIME + ":藐视"),
        Arrays.asList("摸头", CMD.U2_HIME + ":摸摸头"),
        Arrays.asList("魔少", CMD.U2_HIME + ":魔法少女"),
        Arrays.asList("妮可", CMD.U2_HIME + ":niconiconi"),
        Arrays.asList("胖次", CMD.U2_HIME + ":胖次"),
        Arrays.asList("暖被", CMD.U2_HIME + ":暖被窝"),
        Arrays.asList("平摔", CMD.U2_HIME + ":平地摔")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("身检", CMD.U2_HIME + ":身体检查"),
        Arrays.asList("神兽", CMD.U2_HIME + ":四神兽"),
        Arrays.asList("生日", CMD.U2_HIME + ":生日"),
        Arrays.asList("世线", CMD.U2_HIME + ":世界线"),
        Arrays.asList("手办", CMD.U2_HIME + ":你的手办多少钱"),
        Arrays.asList("淑女", CMD.U2_HIME + ":何为淑女"),
        Arrays.asList("推倒", CMD.U2_HIME + ":推倒"),
        Arrays.asList("牛奶", CMD.U2_HIME + ":武藏野牛奶")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("伪娘", CMD.U2_HIME + ":伪娘"),
        Arrays.asList("爱你", CMD.U2_HIME + ":我爱你"),
        Arrays.asList("我头", CMD.U2_HIME + ":我的头"),
        Arrays.asList("我萌", CMD.U2_HIME + ":我好萌"),
        Arrays.asList("回来", CMD.U2_HIME + ":我回来了"),
        Arrays.asList("渴了", CMD.U2_HIME + ":我渴了"),
        Arrays.asList("NTR", CMD.U2_HIME + ":NTR"),
        Arrays.asList("洗澡", CMD.U2_HIME + ":洗澡")
      )),
      Arrays.asList(Arrays.asList(
        Arrays.asList("羞耻", CMD.U2_HIME + ":羞耻play"),
        Arrays.asList("U2娘", CMD.U2_HIME + ":"),
        Arrays.asList("夜神", CMD.U2_HIME + ":夜神"),
        Arrays.asList("雨妹", CMD.U2_HIME + ":雨妹"),
        Arrays.asList("专利", CMD.U2_HIME + ":USPT"),
        Arrays.asList("+1秒", CMD.U2_HIME + ":+1秒"),
        Arrays.asList("巧克", CMD.U2_HIME + ":巧克力"),
        Arrays.asList("用茶", CMD.U2_HIME + ":请用茶")
      ))
    ));
  }

  @Override
  public CMD cmd() {
    return CMD.U2_HIME;
  }

  @Override
  public Boolean needLogin() {
    return true;
  }

  @Override
  public String description() {
    return "U2 娘";
  }

  @Override
  public Message prompt(Long gid) {
    return null;
  }

  private String cacheData(String data) {
    String uuid = UUID.randomUUID().toString();

    HashMap<String, Object> map = new HashMap<>();
    map.put("source", "data");
    map.put("data", data);
    redis.set(uuid, map, Store.TTL);

    return uuid;
  }

}
