#!/bin/bash
# A shell script that outputs a filtered top in given time increments to 
# a text file while a given process runs. This is intended to be used in 
# LVL-2 VMs for capturing process RESs of interest, which are useful for 
# deduplication measurements. The text file will show the final output of top 
# before the process ends. Change the text file name as needed.
# 1st arg = PID of the process

PID=$1
out="LVL2-top.txt"
while kill -0 "$PID" 2>/dev/null; do
  	top -b -n 1 | awk -v pid="$PID" '
   		NR <= 7|| $1 == pid' >> "$out"
     	sleep 1
done
