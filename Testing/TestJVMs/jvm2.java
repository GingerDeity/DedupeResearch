import java.lang.Runtime;
import java.io.*;

public class jvm2 {
	public static void main(String[] args) throws IOException, InterruptedException{
		System.out.println("Hello world!");
		int limit = 25000000;
		Hello hellos[] = new Hello[limit];
		for (int i = 0; i < limit; i++) {
			hellos[i] = new Hello("KILLA");
			//hellos[i] = new Hello("this string will stress the heap more (hopefully), i is now " + i);
		}
		
		if (args.length == 1) {
			long pid = ProcessHandle.current().pid();
			String cmd[] = { "sudo", "gcore", "-o", args[0], Long.toString(pid) };
			Process getCoreDump = Runtime.getRuntime().exec(cmd);
			int exit = getCoreDump.waitFor();
			if (exit != 0) { System.out.println("GCORE error code " + exit + " reported"); }
		}
		else {
			System.out.println("Expected Usage:\n    java jvm2.java [gcore prefix]");
		}
	}
}

class Hello {
	String hello;
	public Hello(String hello) {
		this.hello = hello;
	}
}
