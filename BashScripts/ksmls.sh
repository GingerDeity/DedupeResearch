#!/bin/bash

for file in "/sys/kernel/mm/ksm"/*;
do
	echo "$file:"
	cat "$file"
done
