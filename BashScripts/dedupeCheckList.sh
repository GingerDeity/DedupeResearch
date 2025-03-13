#!/bin/bash
#$1 is window size, $2 will be base file that all others will compare against, $3 will be directory to do DedupeCheck on

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
