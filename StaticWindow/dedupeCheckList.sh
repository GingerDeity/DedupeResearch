#!/bin/bash
# This code outputs static deduplication results and takes in a window size, a 'base' file, 
# and a directory of files to compare against the base. So, if we run this on a directory 
# named 'example' containing files A, B, C, using the command: 
# "bash DedupeCheckList.sh 4096 A example/", this outputs results for: 
# [(A), (A, B), (A, C)]
# $1 is window size, $2 will be base file that all others will compare against, $3 will be directory to do DedupeCheck on

echo "SCANNING $2"
java /mnt/data/atreyu/ParsingTools/DedupeCheck.java $1 "$2"
echo
for file in `ls $3`
do
	if [ "$3$file" != "$2" ];
       	then
		echo "SCANNING $2 $3$file"
		java /mnt/data/atreyu/ParsingTools/DedupeCheck.java $1 "$2" "$3$file"
		echo
	fi
done
