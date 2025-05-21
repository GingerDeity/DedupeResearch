# DedupeResearch
This is the github for as much of my deduplication research as I could fit! There are 3 major folders: VMs, StaticWindow, and KSM. It's worth noting that you should install tmux in your shell environment if you haven't already, as it will be an immensely helpful tool.

## Static Window
This folder contains all code relating to static window experiments, using code I wrote. I'll first go over some of the essential pieces of code, then general experiment processes.

### Essential Code

#### map_and_core
Run with `bash map_and_core.sh $1 $2` where  
* $1 is a process PID  
* $2 is the output files' prefix  

Gets both the memory mapping file and core dump file of a PID

#### elf
This code takes in an ELF CORE file and removes all it's metadata. This is run using `./elf $1 $2` where 
* $1 is the input file
* $2 is the output file name

This code is used before running static deduplication on memory dumps, as memory dumps obtained via `gcore` or `map_and_core.sh` will contain many bytes of metadata, offsetting the data we actually care about. This code removes all the metadata, and ensures the original memory is what's left. When doing static-window deduplication on memory dumps, do it on the files this code outputs.  

#### DedupeCheck.java
The most fundamental piece of code here, it's what actually runs static-window deduplication. This is run using `java DedupeCheck $1 [-v] $2`, where 
* $1 is the size of the window  
* `-v` is a verbose option that outputs information for all matches found  
* $2 is a list of paths to binary memory dumps  

Each match has 2 parts, an "Established match" and a "Discovered match." In other code, these are referred to as the "source" and "copy" of a single match respectively. The "Discovered match" tells you where a complete match has been identified and the "Established match" tells you the original data that has been matched. For instance, if this code first finds a window of data in file A that is then found again in file B, that is a match whose establishment/source is from file A and whose discovery/copy is in file B.  
  
### General Experiment Process
A general experiment will have the following workflow:  
1) Start and run a process (which will have a PID referred to as `PID` from here on)  
2) Some point in the process's life, run `bash map_and_core.sh PID output`, which outputs both the process's memory mapping text file (`output.txt`) and a memory dump binary (`output.PID`)  
3) Run `./elf output.PID output_P.PID`, which will remove all metadata from the `output.PID` binary, resulting in a 'parsed' file  
4) Run `DedupeCheck` with a valid window size and the `output_P.PID` memory dump, which will then finally output static window deduplication results on the processes' memory!  
  
For multiple processes, simply repeat steps 1-3 for each process and then run `DedupeCheck` with all parsed dumps listed!  
  

### Extra Analysis
The following sections describe additional analysis tools for parsed memory dumps, which will generally require a text file containing information from all static-window matches, which can be done through steps 1-3 above, then `java DedupeCheck $1 -v $2 >> output.txt` where `output.txt` will contain the matches information. For simplicity's sake, we'll be referring to this as a 'matches file' from here on.  
  
#### Block Analysis
This code will look at all the static window matches and return a CDF list of the number of matches within certain ranges of bytes from each other. Simply run `python3 blockanalyze.py $1` where  
* $1 is a matches file

#### Parse Matches
This is code is typically used for verifying static-window deduplication output by reporting back how many matches are aligned with a certain user-determined number, or the same size as the user-defined number. For instance, if the user inputs `64`, it reports information about how many matches are 64B-aligned or 64B in size. Run the code using `java ParseMatches $1 $2 $3` where  
* $1 is the window-size/alignment
* $2 is a matches file
* $3 is an optional output file name

#### Map Matches
One of my favorite pieces of code I've written, this can determine what percent of `DedupeCheck` matches come from what memory regions (including heap, stack, shared libraries, anonymous, etc)! Run using `java MapMatches [--assume-parsed] {filename.type: program_headers.txt maps.txt} matches.txt` where
* `matches.txt` is a matches file
* `{filename: program_headers.txt maps.txt}` is repeated for each unique file present in `matches.txt` **(yes, include the curly braces and semicolon)**
  * `filename`: a filename present in `matches.txt`
  * `program_headers.txt`: a text file of program header information for `filename`, produced by running `readelf -l` on `filename`
  * `maps.txt`: a text file of memory mapping information for `filename`, aka the text file output from `map_and_core.sh`
* `[--assume-parsed]` specifies that the files in `matches.txt` are outputs of our `elf` code, meaning they have no metadata. This means the file offsets referred to in `matches.txt` will differ from the file offsets in each `program_headers.txt`, which can result in inaccurate mapping to the memory areas in `maps.txt` if this option is not specified  

### Folder-Wide Deduplication
To quickly do deduplication over an entire folder without listing every file, you can use either `DedupeCheckFull.sh`, `DedupeCheckList.sh`, or `FastFull.sh` (if you have fastcdc installed)

#### DedupeCheckFull
Run with `bash DedupeCheckFull.sh $1 $2` where  
* $1 is the window size  
* $2 will be the directory to do static-window deduplication on  

This code outputs all possible static deduplication results up to two files in a directory. So, for a directory named "example" that contains files A, B, C, running `bash DedupeCheckFull.sh 4096 example/` would output 4KB deduplication results for [(A), (A, B), (A, C), (B), (B, C), (C)]  

#### DedupeCheckList
Run with `bash DedupeCheckList.sh $1 $2 $3` where  
* $1 is the window size  
* $2 will be the base file to compare all others too  
* $3 will be directory to do static-window deduplication on  

This code outputs static deduplication results for a base file compared to all other files in a directory. For instance, if we run this on a directory named 'example' containing files A, B, C, using the command: `bash DedupeCheckList.sh 4096 A example/`, this outputs results for: [(A), (A, B), (A, C)]  

#### Parse Static
This code takes in a text file containing the output of DedupeCheckFull.sh or DedupeCheckList.sh and converts readings into a CSV file, which is very useful for getting a folders' worth of data quickly onto a spreadsheet! Run using `java ParseStatic $1 $2 [-l/-f] $3 $4` where
* $1 is the text file of deduplicaiton result outputs
* $2 is the key, which can be F for Files, DG for Dedupe Graph, UW for Unique Windows, TD for Total Data, DD for Dupe Data, DR for Dedupe Ratio, or ZW for Zero-Windows
* `[-l/-f]` indicates if if the input file was made using DedupeCheckList (`-l`), or DedupeCheckFull (`-f`)
* $3 is the number of unique memory dumps
* $4 is an optional output file name

#### FastFull
Run with `bash FastFull.sh $1` where  
* $1 will be directory to do FastCDC deduplication on  

This code outputs all possible FastCDC deduplication results up to two files in a directory. So, for a directory named "example" that contains files A, B, C, running: `bash FastFull.sh example/` would output FastCDC deduplication results for: [(A), (A, B), (A, C), (B), (B, C), (C)]. You can modify the parameters of the FastCDC deduplication inside the script.  

## KSM
This folder contains all code relating to KSM experiments. I'll first go over the general experiment processes. It's important to note that your Linux kernel will be to be at least version 2.6.32, though later versions will be needed for fields such as `ksm_zero_pages`, which are also crucial for meaningful deduplication data

Some helpful links for learning:  
https://docs.kernel.org/admin-guide/mm/ksm.html
https://docs.kernel.org/mm/ksm.html

### Essential Scripts
#### ksminit
Initializes KSM to not merge zero-pages, scan 500,000 pages at a time, and ensures KSM isn't running. Run with `bash ksminit.sh`  

#### ksmstart
Tells KSM to begin merging pages. Run with `bash ksmstart.sh`  

#### ksmwatch
Prints out all KSM contents to the screen every second, and can be ended with Ctrl+C. Run with `bash ksmwatch.sh`  

#### ksmend
Tells KSM to stop merging pages. Run with `bash ksmend.sh`  

#### ksmls
Prints out all KSM contents to the screen only once. Run with `bash ksmls.sh`  

### General Experiment Process
1) Set up a LVL-1 VM environment for KSM to run from
2) Set at least 1 LVL-2 VM environment for KSM to monitor
3) Set up processes that will run in LVL-2 environment(s)
4) In your LVL-1 VM...  
   a. `bash ksminit.sh` will initialize KSM values  
   b. `bash ksmstart.sh` to start running KSM  
   c. `bash ksmwatch.sh` to observe KSM values  
   d. `bash ksmend.sh` to stop running KSM  