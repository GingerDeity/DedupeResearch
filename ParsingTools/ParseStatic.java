import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

public class ParseStatic {   
    static final int SKIP = 16;
    
    public static void main(String args[]) throws IOException {
        if (args.length != 4 && args.length != 5) {
            System.out.println("INVALID ARGS, Usage:");
            System.out.println("java ParseStatic.java [Input TXT File] [Key Name] "
                + "[-l] [Number of Unique Dumps] [Optional Output File]");
            System.out.println("    -l refers to if the input file was made "
                + "using a List format. Any other char will assume a Full format");
            System.out.print("Key Options:\n");
	    System.out.println("    F=Files              DG=Dedupe Graph");
	    System.out.println("    UW=Unique Windows    TD=Total Data");
	    System.out.println("    DD=Dupe Data         DR=Dedupe Ratio");
	    System.out.println("    ZW=Zero-Windows\n");
	}
        else {
            String key = args[1];
            int linesToSkip = toLines(key);
            if (linesToSkip != -1) {
                Scanner sc = new Scanner(new File(args[0]));
                boolean isList = args[2].equals("-l");
                int dumps = Integer.parseInt(args[3]);
                if (args.length == 5) {
			File toWrite = new File(args[4]);
                	RandomAccessFile writer = new RandomAccessFile(toWrite, "rw");	
			parseFile(sc, linesToSkip, isList, dumps, writer);
			writer.close();
		} 
		else {
			parseFile(sc, linesToSkip, isList, dumps, null);
		}
            }
            else {
                System.out.print("INVALID KEY, Options:\n");
                System.out.println("    F=Files              DG=Dedupe Graph");
                System.out.println("    UW=Unique Windows    TD=Total Data");
                System.out.println("    DD=Dupe Data         DR=Dedupe Ratio");
                System.out.println("    ZW=Zero-Windows\n");
	    }
        }
    }
    
    private static void parseFile(Scanner sc, int linesToSkip, boolean isList, 
        int dumps, RandomAccessFile writer) throws IOException {

        int columns = dumps + 1; 
        int rows;
        if (isList) { rows = 2; } 
        else { rows = dumps + 1; }
        
        String[][] matrix = new String[rows][columns];
        
        matrix[0][0] = "XXXX";
        int xAxisRow = 0;
        int xAxisColumn = 1;
        int yAxisRow = 1;
        int yAxisColumn = 0;
        
        int parsingRow = 1;        
        for (; parsingRow < rows && sc.hasNext(); parsingRow++) {
            int parsingColumn = parsingRow;
            for (; parsingColumn < columns && sc.hasNext(); parsingColumn++) {
                String filesScanned = sc.nextLine();
                
                //Adds header info if necessary
                if (parsingRow == 1) { 
                    StringTokenizer strTokens = new StringTokenizer(filesScanned, " ");
                    strTokens.nextElement(); //Skips the "SCANNING" word
                    int numFiles = strTokens.countTokens();
                    if (numFiles == 2) { strTokens.nextToken(); }
                    String file = strTokens.nextToken();

                    matrix[xAxisRow][xAxisColumn++] = file;
                    if (isList) { 
                        if (numFiles == 1) { matrix[1][0] = file; }
                    }
                    else { matrix[yAxisRow++][yAxisColumn] = file; }
                }
                
                for (int i = 0; i < linesToSkip - 1; i++) { sc.nextLine(); }
                String currLine = sc.nextLine();
                String value = currLine.substring(SKIP);

                if (parsingColumn == parsingRow) {
                    for (int fillIn = 1; fillIn < parsingColumn; fillIn++) {
                        matrix[parsingRow][fillIn] = matrix[fillIn][parsingRow];
                    }
                }
                matrix[parsingRow][parsingColumn] = value;
                
                for (int i = 0; i < 8 - linesToSkip; i++) { sc.nextLine(); }
            }
        }
        
	String output = "";
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                output += matrix[i][j] + ',';
		System.out.print(matrix[i][j] + ", ");
            }
            System.out.println();
	    output += '\n';
        }

	if (writer != null) { writer.writeChars(output); }
    }
    
    private static int toLines(String key) {
        switch (key) {
            case "F":
                return 1;
            case "DG":
                return 2;
            case "UW":
                return 3;
            case "TD":
                return 4;
            case "DD":
                return 5;
            case "DR":
                return 6;
            case "ZW":
                return 7;
            default:
                return -1;
        }
    }
}
