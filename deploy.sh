#!/bin/bash

Green_font_prefix="\033[32m" && Red_font_prefix="\033[31m" && Green_background_prefix="\033[42;37m" && Red_background_prefix="\033[41;37m" && Font_color_suffix="\033[0m"

check_root() {
  if [ `whoami` != "root" ];then
    echo -e "${Red_font_prefix}请使用 ROOT 权限执行脚本${Font_color_suffix}" && exit 1
  fi
}

check_params() {
  if [[ $# -ne 2 ]] ; then
    echo -e "${Red_font_prefix}参数缺失，请指定 -username 和 -token${Font_color_suffix}" && exit 1
  fi
  if [[ $1 =~ "-username=" ]] ; then
    username=`echo ${1: 10}`
  fi
  if [[ $2 =~ "-token=" ]] ; then
    token=`echo ${2: 7}`
  fi
}

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

systemd() {
  touch /etc/systemd/system/u2-bot.service
  cat > "/etc/systemd/system/u2-bot.service"<<-EOF
[Unit]
Description=Telegram Bot - U2 Tool Box
After=network.target

[Service]
Type=simple
WorkingDirectory=/home/
ExecStart=/usr/bin/java -jar /home/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar --bot.username=${1} --bot.token=${2} --phantomjs=phantomjs-2.1.1-linux-x86_64/bin/phantomjs
Restart=always
RestartSec=5s

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable u2-bot
}

download() {
  wget -P /home https://github.com/kurodaak1ra/Telegram-Bot-For-U2/releases/download/0.0.1/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar
  wget -P /home https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2
  tar -jxf /home/phantomjs-2.1.1-linux-x86_64.tar.bz2 -C /home
}

# debian() {
#   echo "正在安装 openjdk8";
#   apt install -y wget gnupg software-properties-common
#   wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
#   add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
#   apt update -y
#   apt install -y adoptopenjdk-8-hotspot
# }

red_hat() {
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
  systemctl start u2-bot
}

check_root
check_params $*
systemd ${username} ${token}

check_sys
# if [[ ${release} == "debian" ]] || [[ ${release} != "ubuntu" ]]; then
#   debian
# fi
if [[ ${release} == "centos" ]]; then
  red_hat
fi
