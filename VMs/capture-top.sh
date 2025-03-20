#!/bin/bash
PID=$1
while kill -0 "$PID" 2>/dev/null; do
	top -b -n 1 > LVL2a-top.txt
	sleep 1
done
