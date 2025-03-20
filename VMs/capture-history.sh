#!/bin/bash
# To be used in LVL-1 VM, KSM-B. This ssh's into both LVL-2 VMs to check if
# given PIDs are still running and, if so, outputs filtered LVL-1 top info and KSM info 
# to output files. Assumes KSM is already initialized and running. Change the
# output file names and pathnames as necessary! The text files will contain a history
# of both KSM and top stats.
# 1st arg = B2a PID
# 2nd arg = B2b PID
# 3rd arg = time between outputs

PID_B2a=$1
PID_B2b=$2
TIME=$3
KSMout="LVL-B1-ksm.txt"
TOPout="LVL-B1-top.txt"
i=0

echo "KSM iterating"
while sudo sshpass -p "admin" ssh -o StrictHostKeyChecking=no josh@localhost -p 10027 "kill -0 $PID_B2a > /dev/null" && \
	sudo sshpass -p "admin" ssh -o StrictHostKeyChecking=no josh@localhost -p 10028 "kill -0 $PID_B2b > /dev/null"; do
  echo >> "$TOPout"
  echo >> "$KSMout"
  echo >> "*** TIME (s) = $((i*TIME)) ***" >> "$TOPout"
  echo >> "*** TIME (s) = $((i*TIME)) ***" >> "$KSMout"  
  top -b -n 1 | awk -v cmd="qemu-system-x86" '
        	NR<=7 || $12 == cmd' > "$TOPout"
	./BashScripts/ksmls.sh > "$KSMout"
	sleep "$TIME"
  ((i++))
done
echo "KSM has ended"
./BashScripts/ksmend.sh
