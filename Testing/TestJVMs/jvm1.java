//import java.lang.Runtime;
//import java.io.*;

public class jvm1 {
	//public static void main(String[] args) throws IOException, InterruptedException {
	public static void main(String[] args) {
		System.out.println("Hello world!");
		while(true) {}
		//if (args.length == 1) {
		//	long pid = ProcessHandle.current().pid();
		//	String[] gcore = { "sudo", "gcore", "-o", args[0], Long.toString(pid) };
		//	Process getDump = Runtime.getRuntime().exec(gcore);
		//	int gcoreExit = getDump.waitFor();
		//	if (gcoreExit != 0) { System.out.println("GCORE error code " + gcoreExit + " reported"); }
		//	
		//	String[] psAux = {"/bin/sh", "-c", "ps aux | grep jvm1" }; //ADDED
		//	Process curr = Runtime.getRuntime().exec(psAux); //ADDED
		//	int psAuxExit = curr.waitFor(); //ADDED
		//	if (psAuxExit != 0) { System.out.println("PS AUX error code " + psAuxExit + " reported"); } //ADDED
		//	BufferedReader reader = new BufferedReader(new InputStreamReader(curr.getInputStream())); //ADDED
		//	System.out.println(reader.readLine()); //ADDED
		//}
		//else {
		//	System.out.println("Expected Usage:\n    java jvm1.java [gcore prefix]");
		//}
	
	}
}
