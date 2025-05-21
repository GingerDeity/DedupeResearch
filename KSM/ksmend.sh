#!/bin/bash
# Tells KSM to finish and unmerge all pages currently merged, but leave mergeable areas registered for next run
# Optionally, you can add PIDs that you want to kill. These PIDs are meant to be processes that were being used by KSM

echo 2 | sudo tee /sys/kernel/mm/ksm/run
for pid in "$@"
do
	sudo kill -9 $pid
done
