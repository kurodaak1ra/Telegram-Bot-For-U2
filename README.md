# Telegram Bot - U2 Tool Box

## 因 PT 站政策原因，无法统一部署到一个 Bot (我猜你们也信不着我)，所以自行部署

### 前期准备
1. 一台电脑或服务器
2. 需部署在可以访问 Google 的网络环境下(国外服务器、国内 IPLC)
3. 自行 @BotFather 申请注册一个属于自己的 TelegramBot，并设置 Bot 的 command 为
```
all - 全部指令
```

### 环境搭建
1. 请在电脑或服务器先安装 JDK (推荐 OpenJDK 8)
```shell
[root@centos ~]# yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
[root@centos ~]# java -version
openjdk version "1.8.0_292"
OpenJDK Runtime Environment (build 1.8.0_292-b10)
OpenJDK 64-Bit Server VM (build 25.292-b10, mixed mode)
```
2. 把打包好的 Jar 包上传到服务器 /home/ 目录下
3. 把项目中的 u2-bot.service 文件上传到服务器 /etc/systemd/system/ 目录下，并修改文件中的 {username} {token} 为你申请到 Bot 的 username 和 token

> telegram-bot-for-u2-0.0.1-SNAPSHOT.jar 参数说明
> 
> 必填参数  
> --bot.username={username} (没有 @)  
> --bot.token={token}
> 
> 选填参数  
> --bot.api={api}  
> --phantomjs={path} (不填没有 PM 提醒功能)
> 
> 可选填 cookie 实现免手动登陆，但不推荐这么做，也不提供设置方法，有能力自行研究

4. 执行命令
```shell
[root@centos ~]# systemctl daemon-reload
[root@centos ~]# systemctl enable u2-bot
[root@centos ~]# systemctl start u2-bot
```

### 非服务器部署，即开即用
(你要上的去 Google 才行
```shell
[root@centos ~]# java -jar telegram-bot-for-u2-0.0.1-SNAPSHOT.jar --bot.username={xxxxx} --bot.token={xxxxx}
```

### 常见问题
> 我的 U2 账号登陆了 Bot 别人会不会也可以操作我的 Bot？

`回答`: 不会，一旦你登陆之后，除了你的 Telegram 账号以外，任何人都操作不了 Bot，直到你登出后 Bot 才会接受别人的指令，并且将 Bot 拉入到群组后 Bot 也会立即自行退出

> 我的 Telegram 账号被注销了，操作不了 Bot 了怎么办

`回答`: 重启服务即可，所有数据都会回归初始

> PhantomJS 如何下载

`回答`: https://phantomjs.org/download.html 选择好平台后直接下载解压，填好可执行文件路径即可，<h3 style="color: red; display: inline">请具体到文件名</h3>
