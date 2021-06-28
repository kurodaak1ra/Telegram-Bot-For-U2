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
  printf "是否使用代理？ [Y/n] "
    read -r proxy_confirmation <&1
    case $proxy_confirmation in
    [yY][eE][sS] | [yY])
      web_proxy=https://startworld.online/
      proxy
      ;;
    [nN][oO] | [nN])
      echo "不使用代理。"
      ;;
    *)
      echo -e "${Red_background_prefix}${proxy_confirmation} 不是有效输入。${Font_color_suffix}\n"
      exit 1
      ;;
  esac
  # 获取最新版本号
  tag=$(wget -qO- -t1 -T2 "${web_proxy}https://api.github.com/repos/kurodaak1ra/Telegram-Bot-For-U2/releases/latest" | grep "tag_name" | head -n 1 | awk -F ":" '{print $2}' | sed 's/\"//g;s/,//g;s/ //g')
  echo -e "即将开始安装 ${Yellow_font_prefix}U2 Tool Box ${tag}${Font_color_suffix}\n"
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
ExecStart=/usr/bin/java -jar /home/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar --bot.username=$username --bot.token=$token --bot.proxy.host=$proxy_host --bot.proxy.port=$proxy_port --bot.proxy.type=$proxy_type --phantomjs.path=/home/phantomjs
Restart=always
RestartSec=5s

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable u2-bot
}

# 代理设置
proxy() {
  echo -e "请选择代理协议:\n\n ${Green_font_prefix}1.${Font_color_suffix} SOCKS4\n ${Green_font_prefix}2.${Font_color_suffix} SOCKS5\n ${Green_font_prefix}3.${Font_color_suffix} HTTP\n"
  read -p "请输入数字 [1-3]: " proxy_type_set
  case "$proxy_type_set" in
    [1]) proxy_type=SOCKS4
        ;;
    [2]) proxy_type=SOCKS5
        ;;
    [3]) proxy_type=HTTP
        ;;
      *) proxy_type=UNKNOWN
        echo -e "${Red_background_prefix} 输入无效，请重新输入 ${Font_color_suffix}"
        read -p "请输入数字 [1-3]: " proxy_type_set
        ;;
  esac
  while [ $proxy_type == "UNKNOWN" ]; do
    case "$proxy_type_set" in
      [1]) proxy_type=SOCKS4
          ;;
      [2]) proxy_type=SOCKS5
          ;;
      [3]) proxy_type=HTTP
          ;;
        *) proxy_type=UNKNOWN
          echo -e "${Red_background_prefix} 输入无效，请重新输入 ${Font_color_suffix}"
          read -p "请输入数字 [1-3]: " proxy_type_set
          ;;
    esac
  done
  echo -e "${Yellow_font_prefix}代理协议:${Font_color_suffix} ${Green_font_prefix}${proxy_type}${Font_color_suffix}\n"

  read -p "请输入代理服务器地址 (IP): " proxy_host
  while [[ ! $proxy_host =~ ^([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$ ]]; do
    read -p "IP 地址输入有误，请重新输入: " proxy_host
  done
  echo -e "${Yellow_font_prefix}代理地址:${Font_color_suffix} ${Green_font_prefix}${proxy_host}${Font_color_suffix}\n"

  read -p "请输入代理服务器端口: " proxy_port
<<<<<<
done
printf "是否添加U2 Hosts？ [Y/n] " 
    read -r hosts_confirmation <&1
    case $hosts_confirmation in
    [yY][eE][sS] | [yY])
        hosts
	      ;;
    [nN][oO] | [nN])
        echo "不添加U2 Hosts"
        ;;
    *)
        echo -e "${Red_background_prefix}${hosts_confirmation} 不是有效输入。${Font_color_suffix}\n"
	      exit 1
	      ;;
esac
}

# U2 Hosts添加
hosts() {
echo -e "# U2 Hosts Start\n104.25.26.31 u2.dmhy.org\n104.25.26.31 tracker.dmhy.org\n104.25.26.31 daydream.dmhy.best\n# Update time: $(date "+%Y-%m-%d %H:%M:%S")">> /etc/hosts
echo "当前使用默认U2 Hosts"
printf "是否使用CloudflareST来测试获取最快Cloudflare IP？ [Y/n] " 
  read -r cfst_confirmation <&1
  case $cfst_confirmation in
  [yY][eE][sS] | [yY])
      CloudflareST
	    ;;
  [nN][oO] | [nN])
      echo "不使用CloudflareST测试"
      ;;
  *)
      echo -e "${Red_background_prefix}${cfst_confirmation} 不是有效输入。${Font_color_suffix}\n"
	    exit 1
	    ;;
  esac
}

# CloudflareST测试程序
CloudflareST() {
  echo -e "${Green_font_prefix}> 正在下载 CloudflareST测试程序${Font_color_suffix}";
  wget -P /home/CloudflareSTtar ${web_proxy}https://github.com/XIU2/CloudflareSpeedTest/releases/download/v1.4.10/CloudflareST_linux_amd64.tar.gz
  tar -zxf /home/CloudflareSTtar/CloudflareST_linux_amd64.tar.gz -C /home/CloudflareSTtar
  mv /home/CloudflareSTtar/CloudflareST /${USER}
  mv /home/CloudflareSTtar/ip.txt /${USER}
  chmod +x /${USER}/CloudflareST
  echo "104.25.26.31" > nowip.txt
	echo -e "${Green_font_prefix}> 开始测速...${Font_color_suffix}";
	NOWIP=$(head -1 nowip.txt)
    ./CloudflareST
	BESTIP=$(sed -n "2,1p" result.csv | awk -F, '{print $1}')
	echo ${BESTIP} > nowip.txt
	echo -e "\n旧 IP 为 ${NOWIP}\n${Yellow_font_prefix}新 IP 为 ${BESTIP}${Font_color_suffix}\n"

	echo "${Green_font_prefix}> 开始备份 Hosts 文件（hosts_backup）...${Font_color_suffix}";
	\cp -f /etc/hosts /etc/hosts_backup

	echo -e "${Green_font_prefix}> 开始替换...${Font_color_suffix}";
	sed -i 's/'${NOWIP}'/'${BESTIP}'/g' /etc/hosts
	echo -e "${Green_font_prefix}> 完成...${Font_color_suffix}";
  echo -e "${Green_font_prefix}> 清理CloudflareST测试程序${Font_color_suffix}";
  rm -rf /home/CloudflareSTtar*
  rm -rf /${USER}/CloudflareST
  rm -rf /${USER}/nowip.txt
  rm -rf /${USER}/ip.txt
=======
  while [[ ! $proxy_port =~ ^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5])$ ]]; do
    read -p "端口输入有误，请重新输入: " proxy_port
  done
  echo -e "${Yellow_font_prefix}代理端口:${Font_color_suffix} ${Green_font_prefix}${proxy_port}${Font_color_suffix}\n"
>>>>>>
}

# 下载 主程序、phantomjs
download() {
  rm -rf /home/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar
  rm -rf /home/phantomjs-2.1.1-linux-x86_64*
  wget -P /home ${web_proxy}https://github.com/kurodaak1ra/Telegram-Bot-For-U2/releases/download/${tag}/telegram-bot-for-u2-0.0.1-SNAPSHOT.jar
  wget -P /home ${web_proxy}https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2
  tar -jxf /home/phantomjs-2.1.1-linux-x86_64.tar.bz2 -C /home
  mv /home/phantomjs-2.1.1-linux-x86_64/bin/phantomjs /home
  rm -rf /home/phantomjs-2.1.1-linux-x86_64*
}
# 下载 中文字体
install_font() {
  mkdir -p /usr/share/fonts/chinese
  rm -f /usr/share/fonts/chinese/SourceHanSansSC-VF.ttf
  wget -P /usr/share/fonts/chinese ${web_proxy}https://github.com/adobe-fonts/source-han-sans/raw/release/Variable/TTF/SourceHanSansSC-VF.ttf /usr/share/fonts/chinese/SourceHanSansSC-VF.ttf
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
