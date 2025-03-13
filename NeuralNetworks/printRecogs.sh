#!/bin/bash

#arg 1 = directory to do this on
#arg 2 = text file for output
for file1 in `ls $1`
do
	echo "Recognizing $file1"
	echo "Recognizing $file1" >> $2
	python3 -Wignore testAlexNet.py "$1/$file1" >> $2
done
