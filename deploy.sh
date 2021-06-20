#!/bin/bash

Green_font_prefix="\033[32m" && Red_font_prefix="\033[31m" && Green_background_prefix="\033[42;37m" && Red_background_prefix="\033[41;37m" && Font_color_suffix="\033[0m"

#判断root用户
check_root() {
  if [ `whoami` != "root" ];then
    echo -e "${Red_font_prefix}请使用 ROOT 权限执行脚本${Font_color_suffix}" && exit 1
  fi
}

#欢迎
welcome()  {
    echo -e '\033[33m
+-------------------------------------------------------------------------------+
 __  __      ___       ______                ___       ____                     
/\ \/\ \   / ___`\    /\__  _\              /\_ \     /\  _`\                   
\ \ \ \ \ /\_\ /\ \   \/_/\ \/   ___     ___\//\ \    \ \ \L\ \    ___   __  _  
 \ \ \ \ \\/_/// /__     \ \ \  / __`\  / __`\\ \ \    \ \  _ <   / __`\/\ \/ \ 
  \ \ \_\ \  // /_\ \     \ \ \/\ \L\ \/\ \L\ \\_\ \_   \ \ \L\ \/\ \L\ \/>  </ 
   \ \_____\/\______/      \ \_\ \____/\ \____//\____\   \ \____/\ \____//\_/\_\
    \/_____/\/_____/        \/_/\/___/  \/___/ \/____/    \/___/  \/___/ \//\/_/
                                                                                                                                                                                  
+-------------------------------------------------------------------------------+
\033[0m'
    echo -e "\033[33m欢迎使用 Telegram Bot - U2 Tool Box 一键安装程序。"
}

#用户输入
check_params() {
  printf "请输入username (没有 @)："
    read -r username <&1
  printf "请输入token："
    read -r token <&1
  echo "安装即将开始"
  echo "如果您想取消安装，"
  echo -e "请在 5 秒钟内按 Ctrl+C 终止此脚本。\033[0m"
  echo ""
  sleep 5
}

#检测系统发行版本
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

#进程守护写入
systemd() {
  touch /etc/systemd/system/u2-bot.service
  cat > "/etc/systemd/system/u2-bot.service"<<-EOF
[Unit]
Description=Telegram Bot - U2 Tool Box
After=network.target

[Service]
Type=simple
WorkingDirectory=/home/
ExecStart=/usr/bin/java -jar /home/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar --bot.username=$username --bot.token=$token --phantomjs=phantomjs-2.1.1-linux-x86_64/bin/phantomjs
Restart=always
RestartSec=5s

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable u2-bot
}

#下载 主程序、phantomjs
download() {
  rm -rf /home/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar
  rm -rf /home/phantomjs-2.1.1-linux-x86_64
  wget -P /home https://github.com/kurodaak1ra/Telegram-Bot-For-U2/releases/download/0.0.1/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar
  wget -P /home https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2
  tar -jxf /home/phantomjs-2.1.1-linux-x86_64.tar.bz2 -C /home
  rm -rf /home/phantomjs-2.1.1-linux-x86_64.tar.bz2
}

# debian_install() {
#   echo "正在安装 openjdk8";
#   apt install -y wget gnupg software-properties-common
#   wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
#   add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
#   apt update -y
#   apt install -y adoptopenjdk-8-hotspot
# }

#CentOS
yum_install() {
  echo -e "${Green_font_prefix}正在安装 openjdk8${Font_color_suffix}";
  yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
  echo -e "${Green_font_prefix}正在安装必要的运行环境${Font_color_suffix}";
  yum install -y wget bzip2 fontconfig
  echo -e "${Green_font_prefix}正在下载 主程序、phantomjs、中文字体${Font_color_suffix}";
  download
  echo -e "${Green_font_prefix}正在安装中文字体${Font_color_suffix}";
  mkdir -p /usr/share/fonts/chinese
  wget -P /usr/share/fonts/chinese https://github.com/adobe-fonts/source-han-sans/raw/release/Variable/TTF/SourceHanSansSC-VF.ttf /usr/share/fonts/chinese/SourceHanSansSC-VF.ttf
  fc-cache
  echo -e "${Green_font_prefix}程序启动${Font_color_suffix}";
  systemctl daemon-reload
  systemctl start u2-bot
}

#根据系统发行版本判断
install_require() {
    #判断为CentOS时执行
    if [ "$release" = "centos" ]; then
        echo "系统检测通过。"
        #用户输入
        check_params
        #进程守护写入
        systemd
        #开始执行CentOS命令
        yum_install
#    elif [ "$release" = "debian" ]; then
#        echo "系统检测通过。"
#        debian
    else
        echo "目前暂时不支持此系统。"
    fi
    exit 1
}


#检测系统发行版本
check_sys
#检测root用户
check_root
#欢迎
welcome
#根据系统发行版本判断执行命令
install_require