# Static Window 
This is a complete walkthrough of nearly all the code found in the StaticWindow folder! Here, we'll be going through the basics of collecting deduplication results, quickly getting CSV files, and more in-depth analysis of files. There are threee memory dumps used throughout this walkthrough, collected by running `./map_and_core.sh` on a `./hello` process (which can be found in the OriginalCode folder). While you're more than welcome to make your own memory dumps using that process, if you're worried about any malfunctioning code then you'll want to use our provided dumps and compare the outputs you get using those to the outputs in the Verify folder. Without further ado, let's begin! Please note that while I am using just the file names of dumps in most examples, you'll need to include the path to them. For instance, if my structure is something like:
```
Home
|-DedupeCheck.java
|-Example
  |-test.bin
```
  
Then the command to have DedupeCheck.java work on the test.bin file will depend on where I'm located. For instance, in my shell environment I'm located in the Home directory, then I'd have to pass in `Example/test.bin`. Keep this in mind for how you'e set up your own environment as you work through the walkthrough.  
  
# Basics
First, unzip the hello1.tar.gz file, and run the command: `elf hello1.2179878 hello1_P.2179878` to remove all the metadata from the hello1 memory dump. You can name the output file anything you like, but I prefer to add the `_P` to denote a dump as 'parsed'. You'll see that the code outputs all sections it writes and a final number showing the new file size. If you want to verify correct output, you can compare this output to the hello1_elf_output.txt inside the Verify folder. Now that we have a working memory dump, let's run deduplication on it!  
  
After compiling your own DedupeCheck.java, run `java DedupeCheck 4096 hello1_P.2179878`, you'll see the following information:  
```
Files:          1
Dedupe Graph:   [0.00%]
Unique Windows: 67
Total Data:     274432 B
Dupe Data:      0 B
Dedupe Ratio:   0.00%
Zero-Windows:   303104 B
```
  
You'll see that `Zero-Windows` + `Duped Data` + `Total Data` will add up to the file size of the memory dump. A low deduplication ratio is to be expected for this, but let's see what happens when we do it with two nearly identical files. Let's now unzip hello2.tar.gz and run the `elf` code on it (which I'll be calling `hello2_P.2179948`). Once that's done, let's try running `java DedupeCheck 4096 hello1_P.2179878 hello2_P.2179948`. You should see the following results:  
```
Files:          2
Dedupe Graph:   [0.00%, 35.07%]
Unique Windows: 87
Total Data:     548864 B
Dupe Data:      192512 B
Dedupe Ratio:   35.07%
Zero-Windows:   606208 B
```
  
As expected, deduplication ratios are much higher! Here's what it looks like if we unzip and parse hello3, then do the following: `java DedupeCheck 4096 hello1_P.2179878 hello2_P.2179948 hello3_P.2179967`
```
Files:          3
Dedupe Graph:   [0.00%, 35.07%, 47.52%]
Unique Windows: 106
Total Data:     827392 B
Dupe Data:      393216 B
Dedupe Ratio:   47.52%
Zero-Windows:   905216 B
```
  
Which essentially translates to "50% of all 3 files is duplicate data that can be compressed" not bad!  
  
# Folder Wide Deduplication and CSVs
Now for a tutorial on quickly getting CSV files. Place the 3 parsed dumps into their own directory. I'm placing mine in a directory called 'test'. Go into your DedupeCheckFull.sh and DedupeCheckList.sh and make sure they point to the correct location of your `DedupeCheck.java` file! After that, run `./DedupeCheckFull.sh 4096 test/` and you'll see that it's outputting matches for up to 2 files! To verify output, compare against the dedupe_full_output.txt file included inside the Verify folder. Let's use that text file and run `java ParseStatic Verify/dedupe_full_output.txt DR -f 3`. You should see the following output:  

```
XXXX, hello1_P.2179878, hello2_P.2179948, hello3_P.2179967,
hello1_P.2179878, 0.00%, 35.07%, 34.81%,
hello2_P.2179948, 35.07%, 0.00%, 36.30%,
hello3_P.2179967, 34.81%, 36.30%, 0.00%,
```
  
Bam! A quick CSV file containing all the deduplication ratios from comparing 2 files. You'll notice that the major diagonal shows the results for 1 file instead of 2. You shouldn't expect to find anything interesting from comparing 2 exact copies of a file. That will always result in ratios of at least 50%. Let's now say we want a CSV file of hello2_P against all the other files, which I've already attached a text file of inside the Verify folder. This can be done by running `./DedupeCheckList.sh 4096 test/hello2_P.2179948 test/`, and then `java ParseStatic Verify/dedupe_list_output.txt DR -l 3` to get the following output:
```
XXXX, test/hello2_P.2179948, test/hello1_P.2179878, test/hello3_P.2179967,
test/hello2_P.2179948, 0.00%, 35.07%, 36.30%,
```
  
You see it automatically starts with the 1 file test, then does the others in order! These are some very handy tools when used right.  
  
# Extra Analysis
For this part, assuming your parsed files are still in `/test`, let's run the following command: `java DedupeCheck 4096 -v test/hello1_P.2179878 test/hello2_P.2179948 >> hello1vhello2_matches.txt`. This file will be necessary for the next steps, and you can make sure you've gotten the correct file by comparing it to the file of the same name inside the Verify folder.  
  
Let's say that perhaps after making some modification to `DedupeCheck.java` we're nervous about if we messed up some of the code, but don't want to parse through all the matches in the matches file to determine this. One simple way is to run `java ParseMatches 4096 hello1vhello2_matches.txt`, which will report back statistics on how many of our matches are 4kB-aligned and 4kB-long. After running this, you should see:  
```
4096B-Aligned-Matches: 47
Total Matches: 47
Average Aligned-Match Size: 4096.00 B
Average Match Size: 4096.00 B
4096B-Aligned & 4096B-Length Matches
Contained THROUGHOUT: 47
Aligned/Total Ratio: 100.00 %
```
  
This tells us there are 47 matches that are both 4kB in terms of alignment and length, and that the code detected 47 matches total. Recall when we did our hello1 vs hello2 deduplication from earlier, there were 192512 bytes of duplicate data. Each match should be 4kB long, so 192512/4096 = 47 matches total. This helps show us our modification didn't destroy any functionality! Let's say we were curious how many of these matches happened to be 8kB aligned, just run `java ParseMatches 8192 hello1vhello2_matches.txt` and you should see:
```
8192B-Aligned-Matches: 25
Total Matches: 47
Average Aligned-Match Size: 4096.00 B
Average Match Size: 4096.00 B
8192B-Aligned & 8192B-Length Matches
Contained THROUGHOUT: 0
Aligned/Total Ratio: 53.19 %
```
  
Which tells us that of all the matches scanned, 25/47 of them were 8kB aligned!  
  
Now let's say we want to see the distances between matching blocks across hello1. We will need a 64B deduplication report of hello1, so run `java DedupeCheck 64 -v test/hello1_P.2179878 >> hello1_matches64.txt` (there's a copy of this file in Verify that you can compare against if needed). From there, just run `python3 blockanalyze.py hello1_matches64.txt` and see:
```
Where x represents distances between pairs of duplicate blocks
Distance Range : Number of duplicate block pairs within Distance Range
x < 2^6 B: 0.0000
2^6 B <= x < 2^7 B: 0.4444
2^7 B <= x < 2^8 B: 0.4444
2^8 B <= x < 2^9 B: 0.4444
2^9 B <= x < 2^10 B: 0.4444
2^10 B <= x < 2^11 B: 0.4444
2^11 B <= x < 2^12 B: 0.7778
2^12 B <= x < 2^13 B: 0.7778
2^13 B <= x < 2^14 B: 0.8889
2^14 B <= x < 2^15 B: 0.8889
2^15 B <= x < 2^16 B: 0.8889
2^16 B <= x < 2^17 B: 1.0000
Total matches parsed:  9
```
   
This shows us the distribution of distances between matching blocks in hello1. For instance, the line `2^6 B <= x < 2^7 B: 0.4444` tells us that 44.44% of all matches were 64 bytes apart from each other! The next 4 lines don't add to that percent, so we know that there were no matching blocks that were 128, 256, 512, or 1024 bytes apart from each other. However, the line `2^11 B <= x < 2^12 B: 0.7778` shows us that some matches were 2048 bytes apart from each other. It essentially tells us 77.78% of all matches were less than 4096 bytes from each other, and doing basic arithmetic tells us that 33.34% of matches were exactly 2048 bytes apart from each other.  
  
Now for what is the most incredible code, **determining what matches come from what memory.** Let's say we want to see what memory our original hello1 vs hello2 experiment will show. For this, we'll need a lot of information, but fortunately we have most of it already. The only new information we need is program header information for the 2 memory dumps of interest. To get this, we need to use the *original* memory dumps, not the parsed one, so run `readelf -l hello1.2179878 >> hello1_pHdrs.txt` and `readelf -l hello2.2179948 >> hello2_pHdrs.txt`. Now for the final command: `java MapMatches --assume-parsed {test/hello1_P.2179878: hello1_pHdrs.txt hello1_maps.txt} {test/hello2_P.2179948: hello2_pHdrs.txt hello2_maps.txt} hello1vhello2_matches.txt`  

This is a lot, so let's go through it bit by bit. Remember, we use `--assume-parsed` because in our matches file the memory dumps were parsed, unlike the dumps we had to use to get the necessary program header files. For each dump present in our matches file, we have a set of curly braces. For filenames, we use `test/hello1_P.2179878` and `test/hello2_P.2179948` because these are the names of the dumps present in our matches file. Then, in each curly braces we input the program header document, then the memory mapping document (the very ones that `map_and_core` gave us at the start). Finally, we simply pass in our matches file. This code will scan that matches file, using the memory dump filenames to correlate program headers with memory areas, and using that information to determine what memory our matches are referencing. Since these are small memory dumps, we will see a small amount of information, but usually these lists are **far** bigger. Here's the expected output:  
```
===Core File 1 Operations Commencing===
    Extracting program header data...
    Parsed file base offset is 1016 B
    Mapping program headers to memory mapped areas...
    ALL program headers of type LOAD map to /proc/PID/maps/ entries

===Core File 2 Operations Commencing===
    Extracting program header data...
    Parsed file base offset is 1016 B
    Mapping program headers to memory mapped areas...
    ALL program headers of type LOAD map to /proc/PID/maps/ entries

===Final Mapping Operations Commencing===
    Mapping matches data to mapped headers...

    All matches where source and copy do NOT share the same mapped name:
    [COPY]   Copy-Filename : Copy-Mapname [Copy-Flags]
        [SOURCE] Source-Filename : Source-Mapname [Source-Flags]
        [TOTAL]  Mismatched-Bytes (%-of-mismatches)


===All Operations Complete===
    Source-Filename : Source-Mapname [Source-Flags] : Matched-Bytes (%-of-matches)

    anon [RW ]: 12288 Bytes (6.38% of matches)
    [vdso] [R E]: 8192 Bytes (4.26% of matches)
    [heap] [RW ]: 4096 Bytes (2.13% of matches)
    /usr/lib/x86_64-linux-gnu/libc.so.6 [R  ]: 163840 Bytes (85.11% of matches)
    /usr/lib/x86_64-linux-gnu/ld-linux-x86-64.so.2 [R  ]: 4096 Bytes (2.13% of matches)

===Final Statistics===
    192512 bytes duped
    0 bytes where the source's mapped name was NOT the same as the copy's mapped name
```
  
The first 2 sections ("Core File 1/2...") are just 'in-progress' checks, but they do tell you the offset that is accounted for should you specify `--assume-parsed`. After that, there will be information for matches where the source and copy aren't referencing the same memory area. In this example, there aren't any, but the format is shown for when it does. Then, we have the part of most interest, the section that shows information for matches who's halves do reference the same memory region. We can see that 85.11% of all matches (or 163840 bytes total) are from the `libc.so.6` shared library! Lastly, we have 2 numbers, one that shows how many bytes of duplicate data the code found (a sanity check, verifying we scanned all matches), and how many bytes worth of 'mismatches' there were.