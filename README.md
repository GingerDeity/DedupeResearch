# DedupeResearch
This is the github for as much of my deduplication research as I could fit! There are 3 major folders: VMs, StaticWindow, and KSM. It's worth noting that you should install tmux in your shell environment if you haven't already, as it will be an immensely helpful tool.

## Static Window
This folder contains all code relating to static window experiments, using code I wrote. A general experiment will have the following workflow:  
  
### General Experiment Process
1) Start and run a process (which will have a PID referred to as `PID` from here on)  
2) Some point in the process's life, run `map_and_core.sh PID output`, which outputs both the process's memory mapping text file (`output.txt`) and a memory dump binary (`output.PID`)  
3) Run `./elf output.PID output_P.PID`, which will remove all metadata from the `output.PID` binary, resulting in a 'parsed' file  
4) Run `DedupeCheck.java` with a valid window size and the `output_P.PID` memory dump, which will then finally output static window deduplication results on the processes' memory!  

For multiple processes, simply repeat steps 1-3 for each process and then run `DedupeCheck.java` with all parsed dumps listed!