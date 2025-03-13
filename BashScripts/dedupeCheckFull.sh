#!/bin/bash
#$1 is window size, $2 will be directory to do DedupeCheck on

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
