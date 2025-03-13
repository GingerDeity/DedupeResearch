#!/bin/bash

cat /proc/"$1"/maps >> "$2"_maps.txt
sudo gcore -o "$2" "$1"
