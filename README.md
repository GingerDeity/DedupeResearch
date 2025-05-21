# DedupeResearch
This is the github for as much of my deduplication research as I could fit! There are 3 major folders: VMs, StaticWindow, and KSM. It's worth noting that you should install tmux in your shell environment if you haven't already, as it will be an immensely helpful tool.

## Static Window
This folder contains all code relating to static window experiments, using code I wrote.  
  
### General Experiment Process
A general experiment will have the following workflow:  
1) Start and run a process (which will have a PID referred to as `PID` from here on)  
2) Some point in the process's life, run `bash map_and_core.sh PID output`, which outputs both the process's memory mapping text file (`output.txt`) and a memory dump binary (`output.PID`)  
3) Run `./elf output.PID output_P.PID`, which will remove all metadata from the `output.PID` binary, resulting in a 'parsed' file  
4) Run `DedupeCheck.java` with a valid window size and the `output_P.PID` memory dump, which will then finally output static window deduplication results on the processes' memory!  
  
For multiple processes, simply repeat steps 1-3 for each process and then run `DedupeCheck.java` with all parsed dumps listed!  
  
### Extra Analysis
The following sections describe additional analysis tools for parsed memory dumps, which will generally require a text file containing information from all static-window matches, which can be done through `DedupeCheck.java [window size] -v [parsed memory dumps] >> output.txt` where `output.txt` will contain the matches information  
  
#### Block Analysis
This code will look at all the matches from a static window code and return a CDF list of the number of matches within certain ranges of bytes from each other. Simply perform steps 1-3 for a general experiment, then run the necessary command for obtaining a text file of static-window matches, then run `python3 blockanalyze.py output.txt`  
  
#### Folder-Wide Deduplication
To quickly do deduplication over an entire folder without listing every file, you can use either `DedupeCheckFull.sh`, `DedupeCheckList.sh`, or `FastFull.sh` (if you have fastcdc installed)

##### DedupeCheckFull
Run with `bash DedupeCheckFull.sh $1 $2` where  
$1 is the window size  
$2 will be the directory to do static-window deduplication on  

This code outputs all possible static deduplication results up to two files in a directory. So, for a directory named "example" that contains files A, B, C, running `bash DedupeCheckFull.sh 4096 example/` would output 4KB deduplication results for [(A), (A, B), (A, C), (B), (B, C), (C)]  

### DedupeCheckList
Run with `bash DedupeCheckList.sh $1 $2 $3` where  
$1 is the window size  
$2 will be the base file to compare all others too  
$3 will be directory to do static-window deduplication on  

This code outputs static deduplication results for a base file compared to all other files in a directory. For instance, if we run this on a directory named 'example' containing files A, B, C, using the command: `bash DedupeCheckList.sh 4096 A example/`, this outputs results for: [(A), (A, B), (A, C)]  

### FastFull
Run with `bash FastFull.sh $1` where  
$1 will be directory to do FastCDC deduplication on  

This code outputs all possible FastCDC deduplication results up to two files in a directory. So, for a directory named "example" that contains files A, B, C, running: `bash FastFull.sh example/` would output FastCDC deduplication results for: [(A), (A, B), (A, C), (B), (B, C), (C)]. You can modify the parameters of the FastCDC deduplication inside the script.  

