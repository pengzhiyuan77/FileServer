#!/bin/sh
pid=fs_pid
if [ -f "$pid" ]
then
	for line in `cat $pid`
	do
	  #当前进程占内存(单位KB)
    cat /proc/$line/status|grep -e VmRSS
    #当前服务器总内存(单位M)
    free -m | grep Mem | awk '{print $2}'
    #当前进程CPU占有率
    cpuinfo=`ps aux|grep $line|grep -v "grep"|awk '{print $3}'`
		echo $cpuinfo
    #当前服务器CPU空闲百分比
    top -b -n 1 | grep Cpu | awk '{print $5}' | cut -f 1 -d "."
    #当前服务器已用内存(单位M)
    free -m | grep Mem | awk '{print $3}'
	done
else
	echo "$pid 不存在"
fi