#!/bin/bash
# This code takes in a directory to conduct fastcdc deduplication on, and
# outputs all possible deduplication results up to two files. So, for a
# directory named "example" that contains files A, B, C, running:
# "bash dedupeCheckFull.sh 4096 example/" would output 4KB deduplication results for:
# [(A), (A, B), (A, C), (B), (B, C), (C)] 
# The original fastcdc code takes in a directory, so to achieve the effect we have to move
# files in and out of a temporary directory.
# $1 is the directory to do fastcdc on

files=($(ls $1))
str="TEMP/"
mkdir "$1$str"

for ((i=0; i<${#files[@]}; i++))
do
	file1=${files[$i]}
	echo "SCANNING $file1"
	mv "$1$file1" "$1$str"
	fastcdc scan -mi 64 -ma 2048 -s 256 "$1$str"
	echo

	for ((j=i+1; j<${#files[@]}; j++))
	do
		file2=${files[$j]}
		echo "SCANNING $file1 $file2"
		mv "$1$file2" "$1$str"
		fastcdc scan -mi 64 -ma 2048 -s 256 "$1$str"
		echo
		mv "$1$str$file2" "$1"
	done
	mv "$1$str$file1" "$1"
done

rmdir "$1$str"
