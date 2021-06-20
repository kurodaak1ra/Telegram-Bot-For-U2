#!/bin/bash

Green_font_prefix="\033[32m" && Red_font_prefix="\033[31m" && Yellow_font_prefix="\033[33m" && Green_background_prefix="\033[42;37m" && Red_background_prefix="\033[41;37m" && Font_color_suffix="\033[0m"

# 判断 root 用户
check_root() {
  if [ `whoami` != "root" ]; then
    echo -e "${Red_font_prefix}请使用 ROOT 权限执行脚本${Font_color_suffix}" && exit 1
  fi
}

# 欢迎
welcome()  {
  clear
  echo -e $Yellow_font_prefix'+--------------------------------------------------------------------------------+
  __  __      ___       ______                ___       ____
 /\ \/\ \   / ___`\    /\__  _\              /\_ \     /\  _`\
 \ \ \ \ \ /\_\ /\ \   \/_/\ \/   ___     ___\//\ \    \ \ \L\ \    ___   __  _
  \ \ \ \ \\\\/_/// /__     \ \ \  / __`\  / __`\\\\ \ \    \ \  _ <   / __`\/\ \/ \
   \ \ \_\ \  // /_\ \     \ \ \/\ \L\ \/\ \L\ \\\\_\ \_   \ \ \L\ \/\ \L\ \/>  </
    \ \_____\/\______/      \ \_\ \____/\ \____//\____\   \ \____/\ \____//\_/\_\
     \/_____/\/_____/        \/_/\/___/  \/___/ \/____/    \/___/  \/___/ \//\/_/

+--------------------------------------------------------------------------------+

欢迎使用 Telegram Bot - U2 Tool Box 一键安装程序'$Font_color_suffix'\n'
}

# 用户输入
check_params() {
  read -p "请输入 Telegram Bot Username: " username
  while [ ! $username ]; do
    read -p "请输入 Telegram Bot Username: " username
  done
  if [ ${username:0:1} == "@" ]; then
    username=${username:1}
  fi
  read -p "请输入 Telegram Bot Token: " token
  while [ ! $token ]; do
    read -p "请输入 Telegram Bot Token: " token
  done
  echo "安装即将开始"
  echo -e "${Red_background_prefix}如您想取消安装，请在 5 秒内按 Ctrl+C 终止${Font_color_suffix}\n"
  sleep 5
}

# 检测系统发行版本
check_sys() {
  if [[ -f /etc/redhat-release ]]; then
    release="centos"
  elif cat /etc/issue | grep -q -E -i "debian"; then
    release="debian"
  elif cat /etc/issue | grep -q -E -i "ubuntu"; then
    release="ubuntu"
  elif cat /etc/issue | grep -q -E -i "centos|red hat|redhat"; then
    release="centos"
  elif cat /proc/version | grep -q -E -i "debian"; then
    release="debian"
  elif cat /proc/version | grep -q -E -i "ubuntu"; then
    release="ubuntu"
  elif cat /proc/version | grep -q -E -i "centos|red hat|redhat"; then
    release="centos"
  fi
  bit=`uname -m`
}

# 进程守护写入
systemd() {
  touch /etc/systemd/system/u2-bot.service
  cat > "/etc/systemd/system/u2-bot.service"<<-EOF
[Unit]
Description=Telegram Bot - U2 Tool Box
After=network.target

[Service]
Type=simple
WorkingDirectory=/home/
ExecStart=/usr/bin/java -jar /home/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar --bot.username=$username --bot.token=$token --phantomjs=/home/phantomjs
Restart=always
RestartSec=5s

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable u2-bot
}

# 下载 主程序、phantomjs
download() {
  rm -rf /home/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar
  rm -rf /home/phantomjs-2.1.1-linux-x86_64*
  wget -P /home https://github.com/kurodaak1ra/Telegram-Bot-For-U2/releases/download/0.0.1/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar
  wget -P /home https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2
  tar -jxf /home/phantomjs-2.1.1-linux-x86_64.tar.bz2 -C /home
  mv /home/phantomjs-2.1.1-linux-x86_64/bin/phantomjs /home
  rm -rf /home/phantomjs-2.1.1-linux-x86_64*
}

install_font() {
  mkdir -p /usr/share/fonts/chinese
  rm -f /usr/share/fonts/chinese/SourceHanSansSC-VF.ttf
  wget -P /usr/share/fonts/chinese https://github.com/adobe-fonts/source-han-sans/raw/release/Variable/TTF/SourceHanSansSC-VF.ttf /usr/share/fonts/chinese/SourceHanSansSC-VF.ttf
  fc-cache
}

# debian_install() {
#   echo "正在安装 openjdk8";
#   apt install -y wget gnupg software-properties-common
#   wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
#   add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
#   apt update -y
#   apt install -y adoptopenjdk-8-hotspot
# }

# CentOS
yum_install() {
  echo -e "${Green_font_prefix}> 正在安装 openjdk8${Font_color_suffix}";
  yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
  echo -e "${Green_font_prefix}> 正在安装必要的运行环境${Font_color_suffix}";
  yum install -y wget bzip2 fontconfig
  echo -e "${Green_font_prefix}> 正在下载 主程序、phantomjs、中文字体${Font_color_suffix}";
  download
  echo -e "${Green_font_prefix}> 正在安装中文字体${Font_color_suffix}";
  install_font
  systemctl start u2-bot
  echo -e "${Green_font_prefix}> U2 Tool Box 已启动${Font_color_suffix}";
  echo -e "${Yellow_font_prefix}\n启动 Bot: systemctl start u2-bot\n终止 Bot: systemctl stop u2-bot${Font_color_suffix}"
}

# 根据系统发行版本判断
install_require() {
  # 判断为CentOS时执行
  if [ "$release" = "centos" ]; then
    # 用户输入
    check_params
    # 进程守护写入
    systemd
    # 开始执行CentOS命令
    yum_install
#  elif [ "$release" = "debian" ]; then
#    debian
  else
    echo "暂时不支持此系统"
  fi
  exit 1
}


# 检测系统发行版本
check_sys
# 检测root用户
check_root
# 欢迎
welcome
# 根据系统发行版本判断执行命令
install_require
