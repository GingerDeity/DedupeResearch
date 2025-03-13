#include <stdio.h>
#include <elf.h>
#include <stdint.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

//Standard size for a page of computer memory
#define PAGESZ 4096

//Prints an error message, then exits the program
void err_log(char* err_msg);

int main(int argc, char** argv) {
	//Open both the input file and create the output file
	int i_fd = open(argv[1], O_RDONLY);
	int o_fd = open(argv[2], O_RDWR | O_CREAT | O_TRUNC, 0777);
	if (i_fd == -1 || o_fd == -1) {
		close(i_fd);
		close(o_fd);
		err_log("ERROR: could not open one or both files");
	}

	struct stat buff;
	if (fstat(i_fd, &buff) == -1) { err_log("ERROR: could not stat input file"); } //We place info about the file from input fd into the buff object
	uint64_t length = buff.st_size; //Obtains the size of the file
	
	void* elfptr = mmap(NULL, length, PROT_READ, MAP_PRIVATE, i_fd, 0); 
	//Maps length bytes of file content into a page-aligned kernel-chosen address, making it private and read-only, starting at offset 0 in the file with the i_fd descriptor
	
	if (elfptr == MAP_FAILED) { err_log("ERROR: could not map input file memory"); }
	printf("MMAP Addr Range: 0x%lx-0x%lx\n", (uintptr_t) elfptr, (uintptr_t) (elfptr + length));
	close(i_fd); //Input file contents are mapped elsewhere, no longer needed

	Elf64_Ehdr* header = (Elf64_Ehdr*) elfptr; //First thing we expect at the start of an ELF file is an ELF Header, of course
	if (!!memcmp(header->e_ident, ELFMAG, SELFMAG)) { err_log("ERROR: not a proper ELF file"); } //Make sure the magic numbers are present
	int e_phnum = (int) header->e_phnum; //Get the number of program segments
	
	Elf64_Phdr* p_header = (Elf64_Phdr*) (elfptr + header->e_phoff); //A pointer to the pHdr table
	uint64_t total = 0; //Will hold how many bytes we've read total

	for (int i = 0; i < e_phnum; i++) { //This loop will iterate through all the pHdrs present
		Elf64_Phdr* p_header_i = &p_header[i]; //Gets a pointer to the current pHdr
		
		if (p_header_i->p_type == PT_LOAD) { //If it's not of type LOAD, we don't care for it, only LOADs hold actual data for core-type files
			uint64_t filesz = p_header_i->p_filesz; //filesz tells us the actual size of the segment in the file, memsz would only tell us how much should be allocated
			uint64_t start = p_header_i->p_offset; //Find the start offset of actual segment data
			uint64_t end = start + filesz - 1; //Tells us end offset
			total += filesz; //We will be writing this, so we add it to total
			
			if (filesz != 0) { //Check that it's not empty, otherwise print that it's N/A
				if ((filesz % PAGESZ) == 0) { //Check that the size is page-aligned, there's a problem with the file indicating if not
					void* data = (void*) (elfptr + start); //Create pointer to segment data
					uint64_t written = 0; //This will tell us how much of the segment we've actually written
					uint64_t remaining = filesz; //This tells us how much of the segment we have left to write
					printf("===Segment %d: %ld-%ld===\n", i, start, end);
					while (remaining > 0) { //Keep writing while there is some remaining, only really useful when segment size is higher than 1 write call can manage
						ssize_t numwrite = write(o_fd, data, remaining); //Keep it type ssize_t so we can see if -1
						if (numwrite == -1) {
							close(o_fd);
							err_log("ERROR: write call failed");
						}
						remaining -= (uint64_t) numwrite; //We know it's not negative, so cast it properly
						written += (uint64_t) numwrite;
						data += (uint64_t) numwrite; //Increment pointer too
						printf("    Currently written: %ld\n", written);
					}

					printf("    Address we read from: 0x%lx\n", (uintptr_t) (elfptr + start));
					printf("    Segment %d completed:(%ld B detected, %ld B written)\n", 
							i, filesz, written);
					if (written != filesz) { //One final error check to make sure we wrote exactly what we should have
						close(o_fd);
						err_log("ERROR: could not write all bytes to output file"); 
					}
				}
				else {
					printf("Segment %d: NOT %d-multiple (%ld B)\n", i, PAGESZ, filesz);
					err_log("ERROR: ELF file contains bad segment");
				}
			}
			else {
				printf("Segment %d: N/A\n", i);
			}
		}
	}

	close(o_fd); //Finished with the mapping
	printf("Total parsed size: %ld B\n", total);
	
	return 0;
}

void err_log(char* err_msg) {
	perror(err_msg);
	printf("ERRNO: %s\n", strerror(errno));
	exit(EXIT_FAILURE);
}
