#!/bin/bash
# A shell script that outputs a filtered top in given time increments to 
# a text file while a given process runs. This is intended to be used in 
# LVL-2 VMs for capturing process RESs of interest, which are useful for 
# deduplication measurements. The text file will show the history of top 
# before the process ends. Change the text file name as needed.
# 1st arg = PID of the process
# 2nd arg = time between outputs in seconds

PID=$1
TIME=$2
i=0
out="LVL2-top.txt"
while kill -0 "$PID" 2>/dev/null; do
	echo >> "$out"
 	echo "*** TIME (s) = $((i*TIME)) ***"
  	top -b -n 1 | awk -v pid="$PID" '
   		NR <= 7|| $1 == pid' >> "$out"
     	sleep "$TIME"
      	((i++))
done
