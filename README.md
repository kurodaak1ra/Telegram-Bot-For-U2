# Telegram U2 Tool Box Bot

## 因 PT 站政策原因，无法部署到统一一个 Bot，所以项目需自行部署

### 前期准备
1. 一台电脑或服务器
2. 需部署在可以访问 Google 的网络环境下（国外服务器、国内 IPLC）
3. 自行 @BotFather 申请注册一个属于自己的 Bot
4. 设置 Bot 的 command 为
```
captcha - 获取登陆验证码
login - 登陆
logout - 登出
me - 当前用户
transfer - 送金币
transfer_info - 送金币当前队列信息
transfer_cancel - 取消送金币等待队列
```

### 环境搭建
1. 请在电脑或服务器先安装 JDK（推荐 OpenJDK 8）
```shell
[root@centos ~]# yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
[root@centos ~]# java -version
openjdk version "1.8.0_292"
OpenJDK Runtime Environment (build 1.8.0_292-b10)
OpenJDK 64-Bit Server VM (build 25.292-b10, mixed mode)
```
2. 把打包好的 Jar 包上传到服务器 /home 目录下
3. 把项目中的 u2-tool-box.service 文件上传到服务器 /etc/systemd/system/
4. 修改 u2-tool-box.service 文件中的 {username} {token} 为你申请到 Bot 的 username 和 token
5. 执行命令
```shell
[root@centos ~]# systemctl daemon-reload
[root@centos ~]# systemctl enable u2-tool-box
[root@centos ~]# systemctl start u2-tool-box
```

### 常见问题
> 我的 U2 账号登陆了 Bot 别人会不会也可以操作我的 Bot？

`回答`: 不会，一旦你登陆之后，除了你的 Telegram 账号以外，任何人都操作不了 Bot，直到你登出后 Bot 才会接受别人的指令，并且将 Bot 拉入到群组后 Bot 也会立即自行退出

> 我的 Telegram 账号被注销了，操作不了 Bot 了怎么办

`回答`: 重启服务即可，所有数据都会回归初始
