# Parsing Tools

These are some of the most important tools in this suite when it comes to running static-window deduplication. There are multiple 'chains' combining these tools that have been used in lots of different experiments.

## Initialization

### Zero Memory
This code allocates a user-specified number of bytes and fills them with zeroes. Run using `./zero_memory $1` where
* $1 is the number of bytes to zero

This is especially helpful in conjunction with experiments involving virtual machines, as these can contain a great deal of nonzero freed memory at any given. Typically, you would run this code on a virtual machine you're about to measure, zeroing out either all the available memory or all the free memory (both amounts can be seen in `top`).

### ELF
This code takes in an ELF CORE file and removes all it's metadata. This is run using `./elf $1 $2` where 
* $1 is the input file
* $2 is the output file name

This code is used before running static deduplication on memory dumps, as memory dumps obtained via `gcore` will contain many bytes of metadata, offsetting the data we actually care about. This code removes all the metadata, and ensures the actual memory information is what's left. When doing static-window deduplication on memory dumps, do it on the files this code outputs.

## Parsing

### Parse Matches

### Parse Static

### Parse FastCDC
