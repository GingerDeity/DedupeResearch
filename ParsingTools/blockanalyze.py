import sys
import re

# This code will look at all the matches from a static window code
# and return a list of the number of matches within certain ranges of 
# bytes from each other
# Requires a text file containing all the matches from a static window
# deduplication

# Throughout the code, we use "copy" and "source" to address the 
# "Discovered Match" and "Established Match" syntax used in the matches
# file respectively

MAX_SIZE = 59 # the maximum size of our array
START_POWER = 6 # the starting power corresponding to index 0 of our array

# distance_counts is an array who's indices correspond to powers of 2, starting at 
# 2^6. This means our array indices represent a range of 64->2^63.
# Each element in distance_counts represents how many pairs of duplicate blocks 
# had a distance between them that was <= the indices' corresponding power and >
# the previous indices' power.
# For example:
#     If distance_counts[4] = 3, there were 3 pairs of duplicate blocks with 
#     1024 B between the start of one and the start of the other.
# This means 2 duplicate blocks right next to each other will be reported as
# 64 B apart

# Returns the lower bound of a power of 2 for a given positive int n
# For example:
#     Returns 5 for n = 63
#     Returns 6 for n = 64
#     Returns 6 for n = 65
#     Returns 6 for n = 127
#     Returns 7 for n = 128
def upper_power_2(n):
    return n.bit_length() - 1

# Reads n lines of file
def readlines(n, file):
    for i in range(n): file.readline()

def main():
    total_matches = 0
    max_index = -1 # The furthest we have to look into distance_counts at end of code
    saved_blocks = {} # A hashtable containing the latest of each unique duplicate block
    distance_counts = [0] * MAX_SIZE # see note below MAX_SIZE and START_POWER constants

    matches = open(sys.argv[1], "r")
    nextline = matches.readline() # Skip the initial blank line

    # nextline having "Files" inside represents the end of the actual matches data
    while nextline != None and "Files:" not in nextline:
        matches.readline() # Skips the ===Discovered Match=== line

        distance = 0

        # Get the copy data's Hash and Offset
        copy_offset = re.search(r'(\d+)', matches.readline())
        copy_offset = int(copy_offset.group(0))
        readlines(2, matches)
        matches.read(10)
        copy_hash = matches.readline()
        
        # Update distance
        if copy_hash not in saved_blocks:
            # Get the source data's offset
            matches.readline()
            source_offset = re.search(r'(\d+)', matches.readline())
            source_offset = int(source_offset.group(0))

            readlines(3, matches)
            distance = copy_offset - source_offset
        else:
            # Use the block already in the hashtable
            readlines(5, matches)
            distance = copy_offset - saved_blocks[copy_hash]

        # Save the latest block into the hashtable, calculate array
        # index based on distance between blocks
        saved_blocks[copy_hash] = copy_offset
        index = upper_power_2(distance) - START_POWER
        distance_counts[index] += 1

        # Update max_index if needed
        if index > max_index: max_index = index
        nextline = matches.readline()

        total_matches += 1
 
    # Print out the info from the distance_counts array
    print("Where x represents distances between pairs of duplicate blocks")
    print("Distance Range : Number of duplicate block pairs within Distance Range")
    cdf_total = 0
    print("x < 2^{} B: {:.4f}".format(START_POWER, cdf_total))
    for i in range(0, max_index + 1):
        cdf_total += distance_counts[i]/total_matches
        print("2^{} B <= x < 2^{} B: {:.4f}".format((i + START_POWER), (i + 1 + START_POWER), cdf_total))
    print("Total matches parsed: ", total_matches)
    matches.close()

if __name__ == "__main__":
    main()
