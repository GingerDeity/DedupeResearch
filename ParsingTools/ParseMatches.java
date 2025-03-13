import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class ParseMatches {
	public static void main(String[] args) throws IOException {
		final int MOD_SIZE = Integer.parseInt(args[0]);
		Scanner scanner = new Scanner(new File(args[1]));
		
		if (args.length == 3) {
			File toWrite = new File(args[2]);
			RandomAccessFile fileOut = new RandomAccessFile(toWrite, "rw");
			processMatches(MOD_SIZE, scanner, fileOut);
		}
		else {
			processMatches(MOD_SIZE, scanner, null);
		}
	}

	private static void processMatches(final int MOD, Scanner sc, RandomAccessFile out)
		throws IOException {
		long totalMatches = 0;
		long alignedMatches = 0;

		long alignedSizes = 0;
		long totalSizes = 0;

		long modCount = 0;
		
		while (sc.hasNext()) {
			String currWrite = "";
			for (int i = 0; i < 2; i++) { currWrite += sc.nextLine() + "\n"; }
			
			//System.out.println(currWrite);
			String endCheck = currWrite.substring(0, 7);
			if (endCheck.equals("\nFiles:")) { break; }

			String fullOffset = sc.nextLine();
			currWrite += fullOffset + "\n";
			Scanner findOff = new Scanner(fullOffset);
			findOff.next();
			long offset = Integer.parseInt(findOff.next());
			
			String fullLength = sc.nextLine();
			currWrite += fullLength + "\n";
			Scanner findLen = new Scanner(fullLength);
			findLen.next();
			long length = Integer.parseInt(findLen.next());

			for (int i = 0; i < 7; i++) { currWrite += sc.nextLine() + "\n"; }
			
			long end = offset + length;
			if (offset % MOD == 0) { 
				if (out != null) { out.writeChars(currWrite); }

				alignedMatches++;
				alignedSizes += length;
			
				long start = offset;
				while ((start + MOD) <= end) {
					start += MOD;
					modCount++;
				}

			}
			else {
				long aligned_off = offset - (offset % MOD);
				while ((aligned_off + MOD) <= end) {
					if (aligned_off > offset) { modCount++; }
					aligned_off += MOD;
				}
			}

			totalMatches++;
			totalSizes += length;
		}

		System.out.println(MOD + "B-Aligned-Matches: " + alignedMatches);
		System.out.println("Total Matches: " + totalMatches);
		
		double avgAlignedSize = (double) alignedSizes / (double) alignedMatches;
		double avgTotalSize = (double) totalSizes / (double) totalMatches;
		System.out.printf("Average Aligned-Match Size: %.2f B\n", avgAlignedSize);
		System.out.printf("Average Match Size: %.2f B\n", avgTotalSize);

		System.out.println(MOD + "B-Aligned & " + MOD + "B-Length Matches\nContained THROUGHOUT: " + modCount);

		double ratio = 100 * (double) alignedMatches / (double) totalMatches;
		System.out.printf("Aligned/Total Ratio: %.2f", ratio);
		System.out.println(" %");
	}

}
