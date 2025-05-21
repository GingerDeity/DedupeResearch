#!/bin/bash
# Spits out all the values of each file in the KSM directory

for file in "/sys/kernel/mm/ksm"/*;
do
	echo "$file:"
	cat "$file"
done
