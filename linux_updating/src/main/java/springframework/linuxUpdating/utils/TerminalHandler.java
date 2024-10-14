package springframework.linuxupdating.utils;

import java.util.Scanner;

public class TerminalHandler {
	/**
	 * 
	 */
	public static void checkOutputAndWaitForEnterKey(){
		System.out.println("Check the output and press enter to continue");
		Scanner scan = new Scanner(System.in);
		scan.nextLine();
		scan.close();
	}
}
