#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>

int main() {
	size_t size = 4096 * 5; //each call to posix_memalign() allocates five pages
	
	char* memory1;
	char* memory2;
	if (posix_memalign((void**)&memory1, 4096, size) != 0) {
		printf("memalign mem1 didn't work\n");
		return 1;
	}

	if (posix_memalign((void**)&memory2, 4096, size) != 0) {
		printf("memalign mem2 didn't work\n");
		return 1;
	}

	memset(memory1, 'A', size);
	memset(memory2, 'A', size);

	if (madvise(memory1, size, MADV_MERGEABLE) != 0) {
		printf("madvise mem1 didn't work\n");
		return 1;
	}

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
