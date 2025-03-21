/*
 * This code allocates a given amount of bytes and sets it's contents
 * to repeating zeroes. This is intended to be used before beginning
 * experiments on VMs, as a way to remove all freed memory that could
 * be seen as duplicate data.
 * 
 * Compiled with: gcc -o0 -o zero_memory zero_memory.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char** argv) {
	// Checks if 1 arg and it's a proper positive long number
	if (argc == 2 && atol(argv[1]) > 0) {
		long zero_amount = atol(argv[1]);
		int* to_zero_out = (int*) malloc(zero_amount);

		// Checks if we could allocate the requested amount
		if (to_zero_out == NULL) {
			printf("ERROR, could not capture requested amount of bytes");
			return 1;
		}

		// Zeroes out the region, doens't free until permitted
		to_zero_out = memset(to_zero_out, 0, zero_amount);
		printf("Press ENTER to free the memory");
		getchar();
		free(to_zero_out);
	}
	else {
		printf("Intended Usage: %s [BYTES TO ZERO]\n", argv[0]);
	}
	return 0;
}
