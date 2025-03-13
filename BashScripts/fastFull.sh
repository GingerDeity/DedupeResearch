#!/bin/bash
#$1 will be directory to do DedupeCheck on

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
