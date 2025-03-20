#!/bin/bash
PID2a=$1
PID2b=$2
echo "KSM iterating"
while sudo sshpass -p "admin" ssh -o StrictHostKeyChecking=no josh@localhost -p 10027 "kill -0 $PID2a > /dev/null" && \
	sudo sshpass -p "admin" ssh -o StrictHostKeyChecking=no josh@localhost -p 10028 "kill -0 $PID2b > /dev/null"; do
	top -b -n 1 > LVL1-top.txt
	./BashScripts/ksmls.sh > LVL1-ksm.txt
	sleep 2
done
echo "KSM has ended"
./BashScripts/ksmend.sh
