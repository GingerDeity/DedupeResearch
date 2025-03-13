#!/bin/bash
# Similar to ksmls.sh, except this continuously updates as ksm runs!
# Typically used in a combo of ksminit -> ksmstart -> ksmwatch -> ksmend

while true; do
	clear
	for file in "/sys/kernel/mm/ksm"/*; do
		echo "$file:"
		cat "$file"
	done
	sleep 1
done
