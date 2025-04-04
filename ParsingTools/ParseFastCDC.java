/*
 * This code attempts to read the output from fastFull.sh and convert it
 * into a csv file of dedupe ratios. I say "attempts" because as of me
 * writing this comment (3-21-25) it doesn't work anymore. Considering that
 * we've abandoned looking into FastCDC for some time, I haven't tried to fix
 * this, but thought I should leave it in case you need it for later
 */

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.RandomAccessFile;

//args[0] is the input file
////args[1] is the output file
public class ParseFastCDC {
    public static void main(String args[]) throws IOException {
        Scanner sc = new Scanner(new File(args[0]));
        File toWrite = new File(args[1]);
        RandomAccessFile writer = new RandomAccessFile(toWrite, "rw");
        
        //Step 1) get the first dump
        //Step 2) while the next 'first' dump is the same as the old one, add to 
        //the same strings

        sc.next();
        String oldDump = sc.next() + ",";
        String nextDump = "";
        while(sc.hasNext()) {
            String allDumps = oldDump;
            String ratios = oldDump;
            do {
                sc.next();
                allDumps += sc.next() + ",";
                for (int i = 0; i < 7; i++) {
                    sc.nextLine();
                }
                sc.next();
                sc.next();                
                ratios += sc.next() + "%,";
                sc.nextLine();
                sc.nextLine();
                if (sc.hasNext()) { 
                    sc.next();
                    nextDump = sc.next() + ",";
                }
            } while (nextDump.equals(oldDump) && sc.hasNext());
            allDumps += "\n";
            ratios += "\n";
            writer.writeChars(allDumps);
            writer.writeChars(ratios);
            oldDump = nextDump;
            nextDump = "";
        }
        writer.close();
        sc.close();
    }
}
