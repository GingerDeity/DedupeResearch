#!/bin/bash
# Simple shell script that outputs memory mapping info and creates a core file
# 1st arg = PID of interest
# 2nd arg = output name

cat /proc/"$1"/maps >> "$2"_maps.txt
sudo gcore -o "$2" "$1"
