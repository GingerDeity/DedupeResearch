#!/bin/bash

while true; do
	clear
	for file in "/sys/kernel/mm/ksm"/*; do
		echo "$file:"
		cat "$file"
	done
	sleep 1
done
