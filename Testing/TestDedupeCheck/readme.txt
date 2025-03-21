This folder contains 2 core files with duplicate pages, designed to give basic tests
for the DedupeCheck.java code. Here's the original layout of each file, summarized
to each page and their repeated contents:

mytest1 (7 pages)	mytest2 (8 pages)
a			        d
b			        e
a			        f
c			        d
0			        b
0			        0
d			        1
9~			        2
                    9~

So for mytest1, the first page consists of nothing but the hexadecimal character 'a'. 
The only exception to this is the 9~ at the end of each file, this section is full of
the hexdecimal character '9' but it doesn't go to a full page.