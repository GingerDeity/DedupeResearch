import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.security.*;

/*
 * This is one of the most essential pieces of code in the entire suite, capable of performing static-window
 * deduplication with any valid window size! There are several qualities that make this code very handy for 
 * experiments. For starters, it automatically ignores any zero-windows and has a verbose mode that prints all
 * matches it finds! This can be redirected into a text file which can be used for other experiments as well.
 * 
 * Each match has 2 parts, an "Established match" and a "Discovered match." In later works, these are referred to
 * as the "source" and "copy" of a single match respectively. The "Discovered match" tells you where a complete match 
 * has been identified and the "Established match" tells you the original data that has been matched. For instance,
 * if this code first finds a window of data in file A that is then found again in file B, that is a match whose 
 * establishment/source is from file A and whose discovery/copy is in file B.
 */
public class DedupeCheck {
	/**
	 *  
	 * @param args[0] will be the window size
	 * @param args[1] will either be the verbose flag or start the list of names of files to be compared
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 *
	 */
	 public static void main(String[] args) throws IOException {
		final int W_SIZE = Integer.parseInt(args[0]);
		boolean verbose = args[1].equals("-v");
		try {
			compareWindow(args, W_SIZE, verbose);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Represents a window we look through into the file
	 * Includes info about it's offset in the file, it's length, 
	 * file of origin, and hash key
	 */
	private static class Window {
		private long offset;
		private int length;
		private String file;
		private String hash;

		public Window(long offset, int length, String file, String hash) {
			this.offset = offset;
			this.length = length;
			this.file = file;
			this.hash = hash;
		}

		public long getOffset() { return offset; }
		public int getLength() { return length; }
		public String getFile() { return file; }
		public String getHash() { return hash; }
	}

	/**
	 *  
	 * @param args is the complete list of arguments
	 * @param windowSize is the size of the window
	 * @param verbose is a boolean denoting whether or not we want verbose information
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void compareWindow(String[] args, int windowSize, boolean verbose) 
		throws NoSuchAlgorithmException, IOException { 

		// Initialize hashtable that will contain all unique haskeys as keys and their Established Window as values
		Hashtable<String, Window> dedupes = new Hashtable<String, Window> (500, (float) 0.55);
		long bytesDuped = 0; // Total number of bytes duped
		long bytesTotal = 0; // Total number of bytes from non-zero-windows
		long zeroWindowsTotal = 0; // Total number of bytes from zero-windows
		double[] fileDupes = new double[args.length - 1]; // Array containing history of deduped-ratio/file

		// Get the starting index of filenames
		int i = 1;
		if (verbose) { i++; }

		// Determine what the zero-window hashkey will be based on window size
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] zeroWindow = new byte[windowSize];
		byte[] zeroWindowHash = digest.digest(zeroWindow);
		String zeroKey = Base64.getEncoder().encodeToString(zeroWindowHash);

		// Iterate through all the given files
		for (; i < args.length; i++) {
			// Access the file read-only and determine how many windows we'll be reading
			RandomAccessFile file = new RandomAccessFile(args[i], "r");
			long fLength = file.length() / windowSize;
			byte[] currWindow = new byte[windowSize];
			String currKey = "";
			
			// Iterate through the windows in the file
			for (long j = 0; j < fLength; j++) {
				// Read curr window and get hashkey
				file.read(currWindow);
				byte[] currWindowHash = digest.digest(currWindow);
				currKey = Base64.getEncoder().encodeToString(currWindowHash);

				// If it's not a zero-window, do hashtable operations!
				if (!currKey.equals(zeroKey)) {
					Window established = dedupes.get(currKey);
					if (established != null) { // Already exists in hashtable, simply add to bytes duplicated
						bytesDuped += windowSize;
						if (verbose) { // Print out info for both Established and Discovered halves
							System.out.println("\n===Discovered Match===");
							System.out.printf("    Offset: %d B\n", ((long) j) * windowSize);
							System.out.printf("    Length: %d B\n", windowSize);
							System.out.printf("    File: %s\n", args[i]);
							System.out.printf("    Hash: %s\n", currKey);
							System.out.println("===Established Match===");
							System.out.printf("    Offset: %d B\n", established.getOffset());
							System.out.printf("    Length: %d B\n", established.getLength());
							System.out.printf("    File: %s\n", established.getFile());
							System.out.printf("    Hash: %s\n", established.getHash());
						}
					}
					else { // Doesn't exist in table, so we add it in
						String temp = currKey;
						Window addNew = new Window(j * windowSize, windowSize, args[i], temp);
						dedupes.put(temp, addNew);
					}
					bytesTotal += windowSize; // Add to non-zero bytes observed total
				} // If it was a zero-window, add to the zero bytes total
				else {
					zeroWindowsTotal += windowSize;
				}
			}

			// Update dedupe-ratio history and close current file
			int dupes_i = i - 1;
			if (verbose) { dupes_i--; }
			fileDupes[dupes_i] += bytesDuped;
			fileDupes[dupes_i] /= bytesTotal;
			fileDupes[dupes_i] *= 100;
			file.close();
		}
		
		// Output all relevant information
		double dedupeRatio = 100 * (double) bytesDuped / (double) bytesTotal;
		int totalFiles = args.length - 1;
		if (verbose) { totalFiles--; } // Files started at args[2]

		System.out.println("Files:          " + totalFiles);
		System.out.print("Dedupe Graph:   [");
		for (int k = 0; k < totalFiles; k++) {
			if (k != totalFiles - 1) {
				System.out.printf("%.2f", fileDupes[k]);
				System.out.print("%, ");
			} else {
				System.out.printf("%.2f", fileDupes[k]);
				System.out.println("%]");
			}
		}
		System.out.println("Unique Windows: " + dedupes.size());
		System.out.println("Total Data:     " + bytesTotal + " B");
		System.out.println("Dupe Data:      " + bytesDuped + " B");        
		System.out.printf("Dedupe Ratio:   %.2f", dedupeRatio);
		System.out.println("%");
		System.out.println("Zero-Windows:   " + zeroWindowsTotal + " B");
	}
}
