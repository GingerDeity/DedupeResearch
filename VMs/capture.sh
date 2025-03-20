#!/bin/bash
# To be used in LVL-1 VM, KSM-B. This ssh's into both LVL-2 VMs to check if
# given PIDs are still running and, if so, outputs LVL-1 top info and KSM info 
# to output files. Assumes KSM is already initialized and running. Change the
# output file names and pathnames as necessary!

PID_B2a=$1
PID_B2b=$2
KSMout="LVL-B1-ksm.txt"
TOPout="LVL-B1-top.txt"
echo "KSM iterating"
while sudo sshpass -p "admin" ssh -o StrictHostKeyChecking=no josh@localhost -p 10027 "kill -0 $PID_B2a > /dev/null" && \
	sudo sshpass -p "admin" ssh -o StrictHostKeyChecking=no josh@localhost -p 10028 "kill -0 $PID_B2b > /dev/null"; do
	top -b -n 1 > "$TOPout"
	./BashScripts/ksmls.sh > "$KSMout"
	sleep 2
done
echo "KSM has ended"
./BashScripts/ksmend.sh
