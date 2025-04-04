# Bash Scripts
This is a directory filled with a variety of incredibly helpful bash scripts. Some of these commands are used so often, I typically include them in my environment's `.bashrc`. All of these can be executed with the `bash` command, and categories are listed below.

## Utilities
Bash scripts that serve as useful shorthands, such as clearing the screen and listing contents.

### cls
Run with `bash cls.sh`  

A simple utility function that clears the screen, prints out a header, and lists the contents of your current directory. This version was used for when I was doing work in the Heap6 server, though it is especially helpful when you are observing multiple VMs at a time and give each VM a distinct header! In fact, each VM I use has a header.txt file that I use for this exact purpose

### psaux
Run with `bash psaux.sh`  

A simple utility function that lets you more quickly identify certain processes under ps aux, a shorthand for `ps aux | grep $1`

## General Deduplication

### dedupeCheckFull
Run with `bash dedupeCheckFull.sh $1 $2` where  
$1 is the size of the static window  
$2 will be the directory to do static-window deduplication on  

This code outputs all possible static deduplication results up to two files in a directory. So, for a directory named "example" that contains files A, B, C, running `bash dedupeCheckFull.sh 4096 example/` would output 4KB deduplication results for [(A), (A, B), (A, C), (B), (B, C), (C)]  

### dedupeCheckList
Run with `bash dedupeCheckList.sh $1 $2 $3` where  
$1 is the size of the static window  
$2 will be the base file to compare all others too  
$3 will be directory to do static-window deduplication on  

This code outputs static deduplication results for a base file compared to all other files in a directory. For instance, if we run this on a directory named 'example' containing files A, B, C, using the command: `bash dedupeCheckList.sh 4096 A example/`, this outputs results for: [(A), (A, B), (A, C)]  

### fastFull
Run with `bash fastFull.sh $1` where  
$1 will be directory to do FastCDC deduplication on  

This code outputs all possible FastCDC deduplication results up to two files in a directory. So, for a directory named "example" that contains files A, B, C, running: `bash fastFull.sh example/` would output FastCDC deduplication results for: [(A), (A, B), (A, C), (B), (B, C), (C)]. You can modify the parameters of the FastCDC deduplication inside the script.  

### map_and_core
Run with `bash map_and_core.sh $1 $2` where  
$1 is a process PID  
$2 is the output files' prefix  

Gets both the memory mapping file and core dump file of a PID, which is helpful for getting the files necessary for `ParsingTools/MapMatches.java`  


## KSM
These are scripts that are to be used in KSM experiments. A typical experiment with KSM will involve running the scripts in the following order:  
1) `bash ksminit.sh` to initialize the KSM service  
2) `bash ksmstart.sh` to start the KSM service  
3) `bash ksmwatch.sh` to observe the KSM service  
4) `bash ksmend.sh` to end the KSM service  

It's important to note that your Linux kernel will be to be at least version 2.6.32, though later versions will be needed for fields such as `ksm_zero_pages`.

Some helpful links for learning:
https://docs.kernel.org/admin-guide/mm/ksm.html
https://docs.kernel.org/mm/ksm.html

### ksminit
Run with `bash ksminit.sh`  

Initializes KSM to not merge zero-pages, scan 500,000 pages at a time, and ensures KSM isn't running.

### ksmstart
Run with `bash ksmstart.sh`  

Tells KSM to begin merging pages.

### ksmwatch
Run with `bash ksmwatch.sh`  

Prints out all KSM contents to the screen every second, and can be ended with Ctrl+C.

### ksmend
Run with `bash ksmend.sh`  

Tells KSM to stop merging pages.

### ksmls
Run with `bash ksmls.sh`  

Prints out all KSM contents to the screen only once.