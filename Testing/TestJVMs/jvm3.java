import java.lang.Runtime;
import java.io.*;
import java.util.*;
import java.security.*;

public class jvm3 {
	/**
	 *  
	 * @param args[0] will be the window size
	 * @param args[1] will either be the verbose flag or start the list of names of files to be compared
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 *
	 */
	 public static void main(String[] args) throws IOException, InterruptedException {
		final int W_SIZE = Integer.parseInt(args[0]);
		boolean verbose = args[1].equals("-v");
		try {
			compareWindow(args, W_SIZE, verbose);
			long pid = ProcessHandle.current().pid();
			String cmd[] = {"sudo", "gcore", Long.toString(pid) };
			Process getCoreDump = Runtime.getRuntime().exec(cmd);
			int exit = getCoreDump.waitFor();
			if (exit != 0) { System.out.println("GCORE error code " + exit + " reported"); }
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Represents a window we look through into the file
	 * Includes info about it's offset in the file, it's length, 
	 * file of origin, and hash key
	 *
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
	 * @param files is the list of files and the window size
	 * @param windowSize is the size of the window
	 * @param verbose is a boolean denoting whether or not we want verbose information
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
        public static void compareWindow(String[] args, int windowSize, boolean verbose) 
		throws NoSuchAlgorithmException, IOException { 
	        
		Hashtable<String, Window> dedupes = new Hashtable<String, Window> (500, (float) 0.55);
		long bytesDuped = 0;
		long bytesTotal = 0;
		long zeroWindowsTotal = 0;
		
		int i = 1;
		double[] fileDupes = new double[args.length - 1];
	        if (verbose) { i++; }
	        
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] zeroWindow = new byte[windowSize]; //Already initialized to all zeroes
		byte[] zeroWindowHash = digest.digest(zeroWindow);
		String zeroKey = Base64.getEncoder().encodeToString(zeroWindowHash);

		for (; i < args.length; i++) {
			RandomAccessFile file = new RandomAccessFile(args[i], "r");
			long fLength = file.length() / windowSize;
			byte[] currWindow = new byte[windowSize];
			String currKey = "";
			
			for (int j = 0; j < fLength; j++) {
				file.read(currWindow);
				byte[] currWindowHash = digest.digest(currWindow);
				currKey = Base64.getEncoder().encodeToString(currWindowHash);
				
				if (!currKey.equals(zeroKey)) {
					Window established = dedupes.get(currKey);
					if (established != null) {
						bytesDuped += windowSize;
						if (verbose) {
							System.out.println("\n===Discovered Match===");
							System.out.printf("    Offset: %d B\n", ((long) j) * windowSize);
							System.out.printf("    Length: %d B\n", windowSize);
							System.out.printf("    File: %s\n", args[i]);
							System.out.printf("    Hash: %s\n", currKey);

							//If it contains the key, then we would want to extract the
							//value associated w it (window info) for the user's established
							//match info
							System.out.println("===Established Match===");
							System.out.printf("    Offset: %d B\n", established.getOffset());
							System.out.printf("    Length: %d B\n", established.getLength());
							System.out.printf("    File: %s\n", established.getFile());
							System.out.printf("    Hash: %s\n", established.getHash());
						}
					}
					else {
						String temp = currKey;
						Window addNew = new Window(j * windowSize, windowSize, args[i], temp);
						dedupes.put(temp, addNew);
					}
					bytesTotal += windowSize;
				}
				else {
					zeroWindowsTotal += windowSize;
				}
			}

			int dupes_i = i - 1;
			if (verbose) { dupes_i--; }
			fileDupes[dupes_i] += bytesDuped;
			fileDupes[dupes_i] /= bytesTotal;
			fileDupes[dupes_i] *= 100;
			file.close();
		}
		
		double dedupeRatio = 100 * (double) bytesDuped / (double) bytesTotal;
		int totalFiles = args.length - 1;
		if (verbose) { totalFiles--; } //Files started at args 2
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
