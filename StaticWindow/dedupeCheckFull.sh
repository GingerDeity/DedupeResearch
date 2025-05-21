#!/bin/bash
# This code takes in both the window size and a directory to conduct static deduplication on.
# This outputs all possible static deduplication results up to two files. So, for a
# directory named "example" that contains files A, B, C, running:
# "bash dedupeCheckFull.sh 4096 example/" would output 4KB deduplication results for:
# [(A), (A, B), (A, C), (B), (B, C), (C)] 
# $1 is window size, $2 will be the directory to do DedupeCheck on 

files=($(ls $2))

for ((i=0; i<${#files[@]}; i++))
do
	file1=${files[$i]}
	echo "SCANNING $file1"
	java -Xmx80g /mnt/data/atreyu/ParsingTools/DedupeCheck.java $1 "$2$file1"
	echo

	for ((j=i+1; j<${#files[@]}; j++))
	do
		file2=${files[$j]}
		echo "SCANNING $file1 $file2"
		java -Xmx80g /mnt/data/atreyu/ParsingTools/DedupeCheck.java $1 "$2$file1" "$2$file2"
		echo
	done
done
