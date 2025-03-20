#!/bin/bash
# A simple shell script that outputs top to a text file while a process runs
# This is intended to be used in LVL-2 VMs, able to capture process RESs, which
# are useful for deduplication measurements. The test file will show the final RES
# size before the process ends. Change the text file name as needed
# 1st arg = PID of the process

PID=$1
while kill -0 "$PID" 2>/dev/null; do
	top -b -n 1 > LVL2-top.txt
	sleep 1
done
