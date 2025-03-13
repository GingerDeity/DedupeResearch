#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char** argv) {
	if (argc == 2 && atol(argv[1]) > 0) {
		long zero_amount = atol(argv[1]);
		int* to_zero_out = (int*) malloc(zero_amount);
		if (to_zero_out == NULL) {
			printf("ERROR, could not capture requested amount of bytes");
			return 1;
		}
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
