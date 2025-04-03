import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class MapMatches {
    
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
             * Array that will initially contain information from program headers, and will later
             * add in 
             */
            Header[] maps = new Header[400];
            int mapEnd = 0;
            int i = 0;
            if (parsedMatches) { i++; }
            
            // Iterates through the file
            for (; i <= args.length - 3 && errorFree; i += 3) {
		        //Gets the filename, associated pHdr file and memory mapping file
                String fileName = args[i].substring(1, args[i].length() - 1);
                Scanner headers = new Scanner(new File(args[i + 1]));
                String mapsName = args[i + 2].substring(0, args[i + 2].length() - 1);
                Scanner procMaps = new Scanner(new File(mapsName));

                System.out.println("\n===Core File " + ((i / 3) + 1)
                    + " Operations Commencing===");
                System.out.println("    Extracting program header data...");

        		//Gets a hashtable of pHdrs for the fileName, each elem has key=vAddr and value=Header-object
                Hashtable<String, Header> pHdrs = getHeaders(fileName, headers, parsedMatches);

                if (pHdrs != null) { //Verifies pHdrs were successfully collected
                    System.out.println("    Mapping program headers to memory mapped areas...");
                    maps = mapHeaders(pHdrs, procMaps, maps, mapEnd); //Add memory map name to headers and places them in maps[]
                    if (maps != null) {
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
            
	        //By this point we have successfully created an array of Header objects containing their offsets in our file and memory-map names!
            if (errorFree) {
                System.out.println("\n===Final Mapping Operations Commencing===");
                System.out.println("    Mapping matches data to mapped headers...");
                System.out.println("\n    All matches where source and copy do NOT share the same mapped name:");
                Scanner matches = new Scanner(new File(args[args.length - 1]));
                matchStats(matches, maps, mapEnd);
            }
            else {
                System.out.println("Please resolve all the above errors before "
                    + "trying again");
            }
            System.out.println();
        }
        else { //If the argument length isn't what it should be then we send out this message explaining the code
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
    
    //Calculates the ending index for our maps[]
    private static int newEnd(Header[] maps, int start) {
        for (int i = start; i < maps.length; i++) {
            if (maps[i] == null) {
                return i;
            }
        }
        return maps.length;
    }
    
    //Links the matches in the matches file to the pHdr+maps info in the Header[] and prints out all final statistics!
    private static void matchStats(Scanner matches, Header[] maps, int mapEnd) {
        matches.nextLine(); //Skips the first blank line in the matches file
        
	//A hashtable that will contain the number of bytes matched from a source (key is the memory map area name that the source corresponds to)
	Hashtable<String, Long> mappedMatches = new Hashtable<String, Long>
            (500, (float) 0.55);

	//A hashtable that will contain the number of bytes where the source memory area name doesn't match the copy's memory area name
        Hashtable<String, Long> mappedMisMatches = new Hashtable<String, Long>
            (500, (float) 0.55);
        long bytesMisMatched = 0;
        long total = 0;

        while (matches.hasNext()) {
            if (matches.nextLine().equals("===Discovered Match===")) {
		//These lines get (for the copy of the match) the name of the dump-file, the match's offset in the dump-file, and length of the match
                matches.next();
                long copyOffset = Long.parseLong(matches.next());
                matches.nextLine();
                matches.next();
                long bytesMatched = Long.parseLong(matches.next());
                matches.nextLine();
                matches.next();
                String copyFile = matches.next();
                
                nextLine(matches, 3);

		//These lines get (for the source of the match) the name of the dump file and the match's offset in the dump-file offset
                matches.next();
                long sourceOffset = Long.parseLong(matches.next());
                nextLine(matches, 2);
                matches.next();
                String sourceFile = matches.next();
                nextLine(matches, 3);
                
                total += bytesMatched;

                String sourceKey = null;
                String copyKey = null;
                boolean foundSourceMap = false;
                boolean foundCopyMap = false;
                boolean alreadyFoundSource = false;
                boolean alreadyFoundCopy = false;

                for (int i = 0; i < mapEnd; i++) {
		    //2 variables that are true (for source and copY) if:
		    //1) the filename is the same for both the source/copy and the maps entry
		    //2) the source/copy offset is in range of the map entry's start and end
                    foundSourceMap = maps[i].getFileName().equals(sourceFile)
                        && (maps[i].getOffStart() <= sourceOffset && sourceOffset <= 
                            maps[i].getOffEnd());
                    foundCopyMap = maps[i].getFileName().equals(copyFile)
                        && (maps[i].getOffStart() <= copyOffset && copyOffset <= 
                            maps[i].getOffEnd());
                    
		    //If we found a link between match's source and a map entry
                    if (foundSourceMap && !alreadyFoundSource) {
                        sourceKey = maps[i].getMapName() + " [" + maps[i].getFlags() + "]";
                        alreadyFoundSource = true;
                    }
                    
		    //If we found a link between match's copy and a map entry
                    if (foundCopyMap && !alreadyFoundCopy) {
                        copyKey = maps[i].getMapName() + " [" + maps[i].getFlags() + "]";
                        alreadyFoundCopy = true;
                    }
                    
		    //If we found map entries for both the copy and source, we can stop
                    if (alreadyFoundCopy && alreadyFoundSource) { break; }
                }
                
		//If we couldn't find a source link or copy link
                if (sourceKey == null || copyKey == null) {
                    System.out.println("ERROR, could not map match to any memory area");
                    return;
                }
                
		//If the memory area name doesn't exist in the hashtable, put it in along w the bytes matched so far
                if (!mappedMatches.containsKey(sourceKey)) {
                    mappedMatches.put(sourceKey, bytesMatched);
                }
                else { //Else, add the new bytes matched to the total
                    long bytesMapped = mappedMatches.get(sourceKey);
                    bytesMapped += bytesMatched;
                    mappedMatches.replace(sourceKey, bytesMapped);
                }

		//A mismatch if the match has matched memory from 2 different map names
                if (!sourceKey.equals(copyKey)) {
                    bytesMisMatched += bytesMatched;
                    String misMatchKey = "[COPY]       " + copyFile + " : " + copyKey 
                        + "\n        [SOURCE] " + sourceFile + " : " + sourceKey;

                    if (!mappedMisMatches.containsKey(misMatchKey)) { //Same logic as the mappedMatched hashtable above
                        mappedMisMatches.put(misMatchKey, bytesMatched);
                    }
                    else {
                        mappedMisMatches.replace(misMatchKey, mappedMisMatches.get(misMatchKey) + bytesMatched);
                    }
                }
            }
        }

	//Print out information about the mismatches
        Enumeration<String> iterMis = mappedMisMatches.keys();
        System.out.println("    [COPY]   Copy-Filename : Copy-Mapname [Copy-Flags]\n"
            + "        [SOURCE] Source-Filename : Source-Mapname [Source-Flags]\n        [TOTAL]  Mismatched-Bytes (%-of-mismatches)\n");
        while (iterMis.hasMoreElements()) {
            String key = iterMis.nextElement();
            long misBytes = mappedMisMatches.get(key);
            String percentage = String.format("%.2f", (double) (100 * (double) misBytes/bytesMisMatched));
            System.out.println("    " + key + "\n        [TOTAL]  " + misBytes + " Bytes (" + percentage + "% of mismatches)");
        }
        
	//Print out information about the matches
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
        
	//One final verification that we got data for ALL the matches
        if (total != verifyTotal) {
            System.out.println("ERROR, total amount of bytes duplicated differs "
                + "between what matches.txt (" + total + "B) and what our code "
                + "reports (" + verifyTotal + "B)");
            return;
        }
        
	//Print out final duped amounts
        System.out.println("\n===Final Statistics===");
        System.out.println("    " + total + " bytes duped");
        System.out.println("    " + bytesMisMatched + " bytes where the source's mapped name "
            + "was NOT the same as the copy's mapped name");
    }
    
    //Returns an aray of updated header information (adds memory map names)
    private static Header[] mapHeaders(Hashtable<String, Header> pHdrs, 
        Scanner procMaps, Header[] maps, int mapEnd) throws IOException {
        
        int mapIndex = mapEnd;
        int verifiedSegments = 0;
        while (procMaps.hasNext()) {
            String vAddrRange = procMaps.next(); //Gets vAddr range (1st elem in each maps entry)
            int delim = vAddrRange.indexOf('-'); //Splits range using - delim
            
            if (delim != -1) { //Verifies we had a valid vAddr range format
		//Gets vAddr start and flags and converts them into format our pHdrs can compare against
                String vAddrStart = convertAddr(vAddrRange.substring(0, delim));
                String flags = convertFlags(procMaps.next());
                
		//Gets corresponding pHdr using the converted vAddrStart as the key
                Header header = pHdrs.get(vAddrStart);
                if (header != null && (header.getFlags()).equals(flags)) { //We found an entry
                    next(procMaps, 3); //Skips past the other data, leaving only a memory area name
                    
                    String mappedName = (procMaps.nextLine()).trim(); //Get the memory area name
                    if (mappedName.equals("")) { mappedName = "anon"; }
                    
                    header.setMapName(mappedName); //Update the header in the hashtable
                    if (mapIndex >= maps.length - 1) {
                        maps = expandCap(maps); //Expand the maps[] if needed
                    }
                    maps[mapIndex++] = header; //Add the header to our array
                    verifiedSegments++;
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
        
        if (verifiedSegments == pHdrs.size()) { //Verifies we could map everything
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
    
    //We return a hashtable of program header information based on the pHdr file given
    private static Hashtable<String, Header> getHeaders(String fileName, 
        Scanner headers, boolean subFirstOffset) {
        nextLine(headers, 3);
        next(headers, 2);
        
	//Gets total number of headers from the "There are ??? program headers..." line
        int totalSegments = Integer.parseInt(headers.next());
	int loadSegments = 0;

        Hashtable<String, Header> pHdrs = new Hashtable<String, Header>(500, (float) 0.55);
        
        nextLine(headers, 5);
        long subOffset = 0;
        
        while (headers.hasNext() && loadSegments < totalSegments) {
            String type = headers.next(); //Gets segment type for each entry (e.g. LOAD, NOTE)
            
	    //We only care about LOAD segments, we skip anything else
            if (type.equals("LOAD")) {
                long offStart = Long.parseLong(headers.next().substring(2), 16); ///Gets "Offset" field from entry
                String vAddr = (headers.next()).substring(2);
                if (vAddr.length() == 16 && offStart >= 0) { //Verifies vAddr and offset, returns error message if needed
                    headers.nextLine();
                    String nextLine = headers.nextLine();
                    long offEnd = Long.parseLong(nextLine.substring(19, 35), 16) 
                        + offStart - 1; //Calculates end by doing "FileSiz + Offset - 1"
                    String flags = nextLine.substring(56, 59); //Gets "Flags" field
                    
                    if (offEnd >= 0 && offEnd > offStart && flags.contains("R")) { //Verifies offsets and flags
                        if (loadSegments == 0) { //This is for if the files in the matches.txt files start with this segment data
                            subOffset = offStart; //We'll be subtracting from all segments is --assumed-parsed was on
			    
			    if (subFirstOffset) { //We only care about this info if we're actually parsing files
				System.out.println("    Parsed file base offset is " + subOffset + " B");
                            }
			}
                        
                        if (subFirstOffset) { //Corresponds to if we said in the arguments "--assume-parsed"
                            offStart -= subOffset; //Now the first program header starts at offset 0, just like in our memory dumps in matches.txt
                            offEnd -= subOffset; //Same goes for the ends too, our memory dumps were parsed so the offsets need to reflect this
                        }
                        
                        Header currHeader = new Header(fileName, vAddr, offStart, offEnd, flags); //Create new Header object
                        pHdrs.put(vAddr, currHeader); //Add to the hashtable, using the vAdr as the key
                        loadSegments++; //Add to the laod segments
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
                nextLine(headers, 2);
            }
        }
        
        //For core files, we expect the number of load segments to be total segments - 1
        if (loadSegments == totalSegments - 1) {
            return pHdrs;
        }
        else {
            System.out.println("ERROR, pHdr file contains unexpected number of "
                + "LOAD type segments");
            return null;
        }
    }

    //Expand the maps[] if needed
    private static Header[] expandCap(Header[] maps) {
        Header[] newMaps = new Header[maps.length * 2];
        for (int i = 0; i < maps.length; i++) {
            newMaps[i] = maps[i];
        }
        return newMaps;
    }
    
    //Converts vAddr in maps file to format from pHdr file
    private static String convertAddr(String vAddr) {
        String leadZeros = "";
        for (int i = 0; i < 16 - vAddr.length(); i++) {
            leadZeros += "0";
        }
        return (leadZeros + vAddr);
    }
    
    //Converts flags from maps file to format from pHdr
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

    //Shorthand function that lets us skip multiple lines
    private static void nextLine(Scanner sc, int lines) {
        for (int i = 0; i < lines && sc.hasNext(); i++) {
            sc.nextLine();
        }
    }
    
    //Shorthand function that lets us skip multiple tokens
    private static void next(Scanner sc, int delims) {
        for (int i = 0; i < delims && sc.hasNext(); i++) {
            sc.next();
        }
    }
    
    //A class that corresponds to a file's program headers
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

}
