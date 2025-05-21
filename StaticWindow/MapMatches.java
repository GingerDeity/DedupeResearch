import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

/**
 * Easily one of my favorite pieces of code I've written for this research. I'm pretty sure it's also
 * the biggest piece of code I've written for this job (though definitely not as efficient as it could be, feel free
 * to upgrade!). This is code takes in three different types of files:
 * 
 * 1) a file containing all the matches from running DedupeCheck.java [window-size] -v [input] >> [output].txt
 * 2) a file containing all the program header information by running readelf -l on a core file used in DedupeCheck.java
 * 3) a file containing all the memory mapping information by running cat /proc/PID/maps while the process that had a 
 *    core dump taken of it for DedupeCheck.java was running
 * 
 * What this code does is determine what percent of matches from a run of DedupeCheck come from what memory regions! 
 * This includes regions such as heap, stack, shared libraries, anonymous, and more. The way it works is by first collecting
 * all the program header information and placing it in an array, then it compares the stored program headers to memory
 * regions in the mapping file, and connects program headers to regions if they share the same virtual address. Finally, we 
 * find the memory region a match is found in based on it's file offset. At the end of it all, you have a comprehensive
 * analysis of how many matches come from what parts of memory. This code also works whether the core dump files in matches 
 * were parsed by elf.c or not! Here's the help message:
 * 
 * Intended usage: javac MapMatches [--assume-parsed] {filename.type: program_headers.txt proc/PID/maps.txt} matches.txt
 * Where each set of curly braces is repeated for each unique file present in the matches.txt file 
 * 
 * {filename: program_headers.txt proc/PID/maps.txt}
 *     filename: a filename present in matches.txt
 *     program_headers.txt: a text file corresponding to the file named, produced from 'readelf -l'
 *     proc/PID/maps.txt: a text file corresponding to the file named, produced from 'cat /proc/PID/maps'
 * 
 * PLEASE NOTE: the filenames present in matches.txt may not be the same as the filenames used for creating the two other 
 *              text files. This is most common when deduplication was run on ELF files that had their metadata removed but 
 *              the other two files were made using untouched ELF files
 * 
 * [--assume-parsed]
 *     Assumes the ELF core files in matches.txt are parsed, meaning the files have no metadata and start with program segment data.
 *     This means the file offsets referred to in matches.txt will differ from the file offsets in each program_headers.txt, which can
 *     result in inaccurate mapping to memory areas in proc/PID/maps.txt. This option addresses that inaccuracy
 */
public class MapMatches {
    
    /**
     * Runs all the different parts of the code, checking for any potential errors
     * at each step of execution
     * @param args contains all the files needed for running
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        boolean errorFree = true;
        boolean parsedMatches = false;
        
       	// Checks if the files have been parsed already	
        if (args.length >= 1 && args[0].equals("--assume-parsed")) {
            parsedMatches = true;
        }

    	// Checks that the argument length is correct
        if ((args.length % 3 == 1 && args.length >= 4) || 
            (parsedMatches && args.length % 3 == 2 && args.length >= 5)) {

            /**
             * Array that will contain information from program headers linked with 'map names', names that 
             * correlate to the memory region the program header was connected to. Initial size is 400 headers
             */
            Header[] maps = new Header[400];
            int mapEnd = 0; // Represents the actual end of the array
            int i = 0;
            if (parsedMatches) { i++; }
            
            // Iterates through the files
            for (; i <= args.length - 3 && errorFree; i += 3) {

                // Gets a filename found in matches.txt and it's associated pHdr file and memory mapping file
                String fileName = args[i].substring(1, args[i].length() - 1); // Removes '{' and ':'
                Scanner headers = new Scanner(new File(args[i + 1]));
                String mapsName = args[i + 2].substring(0, args[i + 2].length() - 1); // Removes '}'
                Scanner procMaps = new Scanner(new File(mapsName));

                // Tells user which core file we're operating on
                System.out.println("\n===Core File " + ((i / 3) + 1)
                    + " Operations Commencing===");
                System.out.println("    Extracting program header data...");

        		// Gets a hashtable of pHdrs for the fileName, where each elem has key=vAddr and value=Header-object
                Hashtable<String, Header> pHdrs = getHeaders(fileName, headers, parsedMatches);

                if (pHdrs != null) { // Verifies pHdrs were successfully collected
                    System.out.println("    Mapping program headers to memory mapped areas...");
                    maps = mapHeaders(pHdrs, procMaps, maps, mapEnd); // Add memory map name to headers inside pHdrs and places them in maps[]
                    if (maps != null) { // Verifies map names were successfully collected
                        mapEnd = newEnd(maps, mapEnd); //Updates the actual end of the maps[]
                    }
                    else {
                        errorFree = false;
                    }
                }
                else {
                    errorFree = false;
                }
            }
            
	        // By this point we have successfully created an array of Header objects containing their offsets in our file and memory-map names!
            if (errorFree) {
                System.out.println("\n===Final Mapping Operations Commencing===");
                System.out.println("    Mapping matches data to mapped headers...");
                System.out.println("\n    All matches where source and copy do NOT share the same mapped name:");
                Scanner matches = new Scanner(new File(args[args.length - 1])); // Scanner for the matches file
                matchStats(matches, maps, mapEnd);
            }
            else {
                System.out.println("Please resolve all the above errors before "
                    + "trying again");
            }
            System.out.println();
        }
        else { // If the argument length isn't what it should be then we send out this message explaining the code
            String startingSyntax = 
                  "Intended usage:\njava MapMatches.java [--assume-parsed] {filename.type: program_headers.txt proc/PID/maps.txt} matches.txt\n"
                + "    Where each set of curly braces is repeated for each unique file present in the matches.txt file.\n";
            String curlyBraces = 
                  "\n{filename: program_headers.txt proc/PID/maps.txt}\n"
                + "    filename: a filename present in matches.txt,\n"
                + "    program_headers.txt: a text file corresponding to the file named, produced from 'readelf -l',\n"
                + "    proc/PID/maps.txt: a text file corresponding to the file named, produced from 'cat /proc/PID/maps',"
                + "\n\n    PLEASE NOTE: the filenames present in matches.txt may not be the same as the filenames used for"
                + " creating the two other text files.\n        This is most common when deduplication was run on ELF"
                + " files that had their metadata removed but the other two files were made"
                + "\n        using untouched ELF files\n";
            String assumeParsed = "\n[--assume-parsed]\n    Assumes the "
                + "ELF core files in matches.txt are parsed, meaning the files have "
                + "no metadata and start with program segment data.\n    This means "
                + "the file offsets referred to in matches.txt will differ from the "
                + "file offsets in each program_headers.txt,\n    which can result in "
                + "inaccurate mapping to memory areas in proc/PID/maps.txt . This option "
                + "addresses that inaccuracy";

            System.out.println(startingSyntax + curlyBraces + assumeParsed);
        }
    }

    
    /**
     * A class that corresponds to a file's program headers and memory mapping information.
     * In getHeaders(), we fill these with program header information, and later in mapHeaders(), 
     * we add in the mapNames.
     */
    private static class Header {
        private String fileName;
        private String mapName;
        private String flags;
        private String vAddr;
        private long offStart;
        private long offEnd;
        
        public Header(String fileName, String vAddr, long offStart, long offEnd, String flags) {
            this.fileName = fileName;
            this.flags = flags;
            this.vAddr = vAddr;
            this.offStart = offStart;
            this.offEnd = offEnd;            
        }

        public String getFileName() { return fileName; }
        public String getMapName() { return mapName; }
        public long getOffStart() { return offStart; }
        public long getOffEnd() { return offEnd; }
        public String getFlags() { return flags; }
        public String getVaddr() { return vAddr; }

        public void setFileName(String fileName) { this.fileName = fileName; }
        public void setMapName(String mapName) { this.mapName = mapName; }
        public void setOffStart(long offStart) { this.offStart = offStart; }
        public void setOffEnd(long offEnd) { this.offEnd = offEnd; }
        public void setFlags(String flags) { this.flags = flags; }
        public void setVaddr(String vAddr) { this.vAddr = vAddr; }
    }


    /**
     * We return a hashtable of program header information based on the pHdr file given
     * @param fileName is the name of a file found in matches.txt
     * @param headers is the scanner for that file's associated pHdr file
     * @param subFirstOffset tells us whether the matches file was stripped of metadata
     * @return a hashtable of program headers, where key=vAddr and value=Header
     */
    private static Hashtable<String, Header> getHeaders(String fileName, Scanner headers, boolean subFirstOffset) {
        // Skips unnecessary info from 'readelf -l' 
        nextLine(headers, 3);
        next(headers, 2);
        
    	// Gets total number of headers from the "There are ??? program headers..." line
        int totalSegments = Integer.parseInt(headers.next());
	    int loadSegments = 0;

        // Initializes the hashtable we'll be returning
        Hashtable<String, Header> pHdrs = new Hashtable<String, Header>(500, (float) 0.55);
        
        nextLine(headers, 5);
        long subOffset = 0;
        
        // Iterates through all the headers. In a ELF-CORE file, the number of load segments will be equal to the
        // total number of segments - 1
        while (headers.hasNext() && loadSegments < totalSegments) {
            String type = headers.next(); // Gets segment type for each entry (for CORE files this is either LOAD or NOTE)
            
	        // We only care about LOAD segments, we skip anything else
            if (type.equals("LOAD")) {
                long offStart = Long.parseLong(headers.next().substring(2), 16); // Gets "Offset" field from header entry
                String vAddr = (headers.next()).substring(2); // Gets "VirtAddr" field from header entry

                if (vAddr.length() == 16 && offStart >= 0) { // Verifies vAddr and offset, returns error message if needed
                    headers.nextLine();
                    String nextLine = headers.nextLine();

                    // Calculates end by doing "FileSiz + Offset - 1"
                    long offEnd = Long.parseLong(nextLine.substring(19, 35), 16) + offStart - 1;
                    String flags = nextLine.substring(56, 59); //Gets "Flags" field

                    // Verifies offsets and flags
                    if (offEnd >= 0 && offEnd > offStart && flags.contains("R")) {

                        // Checks if this is the first segment since, if --assumed-parsed was specified, we'll be subtracting this segment's 
                        // offset from all others
                        if (loadSegments == 0) {
                            subOffset = offStart; 
                            if (subFirstOffset) { System.out.println("    Parsed file base offset is " + subOffset + " B"); }
			            }
                        
                        if (subFirstOffset) { // If --assume-parsed was specified
                            offStart -= subOffset; // Now the first program header starts at offset 0, just like in our memory dumps in matches.txt
                            offEnd -= subOffset; // Same goes for the ends too, our memory dumps were parsed so the offsets need to reflect this
                        }
                        
                        Header currHeader = new Header(fileName, vAddr, offStart, offEnd, flags); // Instantiate new Header object
                        pHdrs.put(vAddr, currHeader); // Add to the hashtable, using the VirtAddr as the key
                        loadSegments++; // Increment valid load segments
                    }
                    else {
                        System.out.println("ERROR, pHdr file contains headers with "
                            + "invalid size and/or invalid flags");
                        return null;
                    }
                }
                else {
                    System.out.println("ERROR, pHdr file contains headers with "
                        + "invalid vAddr and/or negative file offset");
                    return null;
                }
            }
            else {
                nextLine(headers, 2); // Not a LOAD segment, so skip it
            }
        }
        
        // For ELF-CORE files, we expect the number of load segments to be total segments - 1
        if (loadSegments == totalSegments - 1) {
            return pHdrs;
        }
        else {
            System.out.println("ERROR, pHdr file contains unexpected number of "
                + "LOAD type segments");
            return null;
        }
    }


    /**
     * Returns an array of updated header information (adds memory map names)
     * @param pHdrs is the hashtable of program header info, whose key=vAddr and value=Header
     * @param procMaps is the scanner for the memory mapping file
     * @param maps is the array to add the updated program headers to
     * @param mapEnd is the real end of the maps array
     * @return an array of program headers with map names
     * @throws IOException
     */
    private static Header[] mapHeaders(Hashtable<String, Header> pHdrs, 
        Scanner procMaps, Header[] maps, int mapEnd) throws IOException {
        
        int mapIndex = mapEnd;
        int verifiedSegments = 0;
        while (procMaps.hasNext()) {
            String vAddrRange = procMaps.next(); // Gets vAddr range (1st elem in each maps entry)
            int delim = vAddrRange.indexOf('-'); // Splits range using - delim
            
            if (delim != -1) { // Verifies we had a valid vAddr range format
        		
                // Gets vAddr start and flags and converts them into a format our pHdrs use
                String vAddrStart = convertAddr(vAddrRange.substring(0, delim));
                String flags = convertFlags(procMaps.next());
                
		        // Gets corresponding pHdr using the converted vAddrStart as the key
                Header header = pHdrs.get(vAddrStart);
                if (header != null && (header.getFlags()).equals(flags)) { // We found a valid entry
                    next(procMaps, 3); // Skips to the memory area name and collects it
                    String mappedName = (procMaps.nextLine()).trim();
                    if (mappedName.equals("")) { mappedName = "anon"; }
                    
                    header.setMapName(mappedName); // Update the header in the hashtable

                    // Expand the maps[] if needed
                    if (mapIndex >= maps.length - 1) { maps = expandCap(maps); }
                    maps[mapIndex++] = header; // Add the header to our array
                    verifiedSegments++; // Increment the number of valid segments
                }
                else {
                    procMaps.nextLine();
                }
            }
            else {
                System.out.println("ERROR, invalid /proc/PID/maps file format, "
                    + "vAddr range invalid");
                break;
            }
        }
        
        if (verifiedSegments == pHdrs.size()) { // Verifies we could map everything
            System.out.println("    ALL program headers of type LOAD map to "
                + "/proc/PID/maps/ entries");
            return maps;
        }
        else {
            System.out.println("ERROR, not all program headers of type LOAD map to "
                + "/proc/PID/maps/ entries");
            return null;
        }
    }


    /**
     * Links the matches in the matches file to the pHdr+maps info in the Header[] and prints out all final statistics!
     * @param matches
     * @param maps
     * @param mapEnd
     */
    private static void matchStats(Scanner matches, Header[] maps, int mapEnd) {
        matches.nextLine(); // Skips the first blank line in the matches file
        
        // Two hashtables, both of which have key="MAP-NAME [FLAGS]"
	    Hashtable<String, Long> mappedMatches = new Hashtable<String, Long>(500, (float) 0.55); // value=number of bytes matched from a source
        Hashtable<String, Long> mappedMisMatches = new Hashtable<String, Long>(500, (float) 0.55); // value=number of bytes where source memory name doesn't match copy's

        long bytesMisMatched = 0; // Total bytes where source memory mapping != copy memory mapping
        long total = 0; // Total bytes observed

        // Iterate through all the matches
        while (matches.hasNext()) {
            if (matches.nextLine().equals("===Discovered Match===")) {
	        	// These lines get copy-match's file, file offset, and length
                matches.next();
                long copyOffset = Long.parseLong(matches.next());
                matches.nextLine();
                matches.next();
                long bytesMatched = Long.parseLong(matches.next());
                matches.nextLine();
                matches.next();
                String copyFile = matches.next();
                
                nextLine(matches, 3);

	        	// These lines get source-match's file, file offset, and length
                matches.next();
                long sourceOffset = Long.parseLong(matches.next());
                nextLine(matches, 2);
                matches.next();
                String sourceFile = matches.next();
                nextLine(matches, 3);
                
                total += bytesMatched; // Add to bytes observed

                String sourceKey = null;
                String copyKey = null;
                boolean foundSourceMap = false;
                boolean foundCopyMap = false;
                boolean alreadyFoundSource = false;
                boolean alreadyFoundCopy = false;

                /* 
                 * Find the header that would contain this match by comparing the match's offset
                 * to the offsets of the header
                 */
                for (int i = 0; i < mapEnd; i++) {
                    /* 
                     * These are 2 variables that are true if:
                     * A) the filename is the same for both the source/copy and the maps entry
                     * B) the source/copy offset is inside the map entry's range
                     */
                    foundSourceMap = maps[i].getFileName().equals(sourceFile)
                        && (maps[i].getOffStart() <= sourceOffset && sourceOffset <= 
                            maps[i].getOffEnd());
                    foundCopyMap = maps[i].getFileName().equals(copyFile)
                        && (maps[i].getOffStart() <= copyOffset && copyOffset <= 
                            maps[i].getOffEnd());
                    
        		    // If we found a link between match's source and a map entry
                    if (foundSourceMap && !alreadyFoundSource) {
                        sourceKey = maps[i].getMapName() + " [" + maps[i].getFlags() + "]";
                        alreadyFoundSource = true;
                    }
                    
		            // If we found a link between match's copy and a map entry
                    if (foundCopyMap && !alreadyFoundCopy) {
                        copyKey = maps[i].getMapName() + " [" + maps[i].getFlags() + "]";
                        alreadyFoundCopy = true;
                    }
                    
		            // If we found map entries for both the copy and source, we can stop for this match
                    if (alreadyFoundCopy && alreadyFoundSource) { break; }
                }
                
        		// If we couldn't find fully link a match to map regions
                if (sourceKey == null || copyKey == null) {
                    System.out.println("ERROR, could not map match to any memory area");
                    return;
                }
                
		        // If the memory area name doesn't exist in the hashtable, put it in along w the bytes matched so far
                if (!mappedMatches.containsKey(sourceKey)) {
                    mappedMatches.put(sourceKey, bytesMatched);
                }
                else { // else, add the new bytes matched to the total
                    long bytesMapped = mappedMatches.get(sourceKey);
                    bytesMapped += bytesMatched;
                    mappedMatches.replace(sourceKey, bytesMapped);
                }

        		// A mismatch if the match has matched memory from 2 different map names
                if (!sourceKey.equals(copyKey)) {
                    bytesMisMatched += bytesMatched;
                    String misMatchKey = "[COPY]       " + copyFile + " : " + copyKey 
                        + "\n        [SOURCE] " + sourceFile + " : " + sourceKey;

                    if (!mappedMisMatches.containsKey(misMatchKey)) { // Same logic as the mappedMatched hashtable above
                        mappedMisMatches.put(misMatchKey, bytesMatched);
                    }
                    else {
                        mappedMisMatches.replace(misMatchKey, mappedMisMatches.get(misMatchKey) + bytesMatched);
                    }
                }
            }
        }

    	// Print out information about the mismatches
        Enumeration<String> iterMis = mappedMisMatches.keys();
        System.out.println("    [COPY]   Copy-Filename : Copy-Mapname [Copy-Flags]\n"
            + "        [SOURCE] Source-Filename : Source-Mapname [Source-Flags]\n        [TOTAL]  Mismatched-Bytes (%-of-mismatches)\n");
        while (iterMis.hasMoreElements()) {
            String key = iterMis.nextElement();
            long misBytes = mappedMisMatches.get(key);
            String percentage = String.format("%.2f", (double) (100 * (double) misBytes/bytesMisMatched));
            System.out.println("    " + key + "\n        [TOTAL]  " + misBytes + " Bytes (" + percentage + "% of mismatches)");
        }
        
    	// Print out information about the matches
        System.out.println("\n===All Operations Complete===");
        System.out.println("    Source-Filename : Source-Mapname [Source-Flags] : Matched-Bytes (%-of-matches)\n");
        long verifyTotal = 0;
        Enumeration<String> iter = mappedMatches.keys();
        while (iter.hasMoreElements()) {
            String key = iter.nextElement();
            long duped = mappedMatches.get(key);
            String percentage = String.format("%.2f", (double) (100 * (double) duped/total));
            System.out.println("    " + key + ": " + duped + " Bytes (" 
                + percentage + "% of matches)");
            verifyTotal += duped;
        }
        
	    // One final verification that we got data for ALL the matches
        if (total != verifyTotal) {
            System.out.println("ERROR, total amount of bytes duplicated differs "
                + "between what matches.txt (" + total + "B) and what our code "
                + "reports (" + verifyTotal + "B)");
            return;
        }
        
    	// Print out final duped amounts
        System.out.println("\n===Final Statistics===");
        System.out.println("    " + total + " bytes duped");
        System.out.println("    " + bytesMisMatched + " bytes where the source's mapped name "
            + "was NOT the same as the copy's mapped name");
    }
        

    /**
     * Recalculates the ending index for our maps[]
     * @param maps is the array to iterate through
     * @param start is the index to start at
     * @return the index of the first null element, or the length of the array
     */
    private static int newEnd(Header[] maps, int start) {
        for (int i = start; i < maps.length; i++) {
            if (maps[i] == null) { return i; }
        }
        return maps.length;
    }


    /**
     * Expand the maps[] if needed
     * @param maps is the array to expand
     * @return the new array
     */
    private static Header[] expandCap(Header[] maps) {
        Header[] newMaps = new Header[maps.length * 2];
        for (int i = 0; i < maps.length; i++) {
            newMaps[i] = maps[i];
        }
        return newMaps;
    }
    

    /**
     * Formats the vAddr from memory maps file into the pHdr file format
     * @param vAddr is the vAddr field from memory mapping file
     * @return vAddr string in the format of pHdr files
     */
    private static String convertAddr(String vAddr) {
        String leadZeros = "";
        for (int i = 0; i < 16 - vAddr.length(); i++) {
            leadZeros += "0";
        }
        return (leadZeros + vAddr);
    }
    

    /**
     * Converts flags from memory mapping file into the format from pHdr files
     * @param flags is the flags field from the memory mapping file
     * @return pHdr formatted flags
     */
    private static String convertFlags(String flags) {
        String result = "R";
        if ((flags.substring(1, 2)).equals("w")) {
            result += "W";
        } else {
            result += " ";
        }

        if ((flags.substring(2, 3)).equals("x")) {
            result += "E";
        } else {
            result += " ";            
        }

        return result;
    }


    /**
     * Shorthand function that lets us skip multiple lines
     * @param sc is the scanner to do the skipping on
     * @param delims is the number of lines to skip
     */
    private static void nextLine(Scanner sc, int lines) {
        for (int i = 0; i < lines && sc.hasNext(); i++) {
            sc.nextLine();
        }
    }
    
    
    /**
     * Shorthand function that lets us skip multiple tokens
     * @param sc is the scanner to do the skipping on
     * @param delims is the number of tokens to skip
     */
    private static void next(Scanner sc, int delims) {
        for (int i = 0; i < delims && sc.hasNext(); i++) {
            sc.next();
        }
    }
}