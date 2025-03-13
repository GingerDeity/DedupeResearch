#!/bin/bash

echo 2 | sudo tee /sys/kernel/mm/ksm/run
for pid in "$@"
do
	sudo kill -9 $pid
done
