/*
 * Code that simply allocates a certain number of pages all sharing the 
 * same data. If KSM is working correctly, it'll pick up on the pages
 * allocated.
 *
 * Compiled with" gcc -o0 -o testKSM testKSM.c
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>

int main() {
	size_t size = 4096 * 50; // each call to posix_memalign() allocates five pages
	char* memory1;
	char* memory2;
	
	// Aligns memory1 to a 4kB address in memory
	if (posix_memalign((void**)&memory1, 4096, size) != 0) {
		printf("memalign mem1 didn't work\n");
		return 1;
	}

	// Aligns memory2 to a 4kB address in memory
	if (posix_memalign((void**)&memory2, 4096, size) != 0) {
		printf("memalign mem2 didn't work\n");
		return 1;
	}

	// Sets the data in strings to repeat 'A'
	memset(memory1, 'A', size);
	memset(memory2, 'A', size);

	// Tells kernel that the memory1 region is KSM-mergeable
	if (madvise(memory1, size, MADV_MERGEABLE) != 0) {
		printf("madvise mem1 didn't work\n");
		return 1;
	}

	// Tells kernel that the memory2 region is KSM-mergeable
	if (madvise(memory2, size, MADV_MERGEABLE) != 0) {
		printf("madvise mem2 didn't work\n");
		return 1;
	}

	printf("Press ENTER to free memory\n");
	getchar();
	free(memory1);
	free(memory2);

	return 0;
}
