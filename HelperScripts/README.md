# Bash Scripts
This is a directory filled with a few very helpful bash scripts. These commands are used so often, I typically include them in my environment's `.bashrc`. All of these can be executed with the `bash` command, and categories are listed below. I also do the same for the KSM bash scripts

## cls
Run with `bash cls.sh`  

A simple utility function that clears the screen, prints out a header, and lists the contents of your current directory. This version was used for when I was doing work in the Heap6 server, though it is especially helpful when you are observing multiple VMs at a time and give each VM a distinct header! In fact, each VM I use has a header.txt file that I use for this exact purpose

## psaux
Run with `bash psaux.sh`  

A simple utility function that lets you more quickly identify certain processes under ps aux, a shorthand for `ps aux | grep $1` where
* $1 is the PID of interest