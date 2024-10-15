package springframework.linuxupdating.utils;

import java.util.Scanner;

import springframework.linuxupdating.model.CommandSet;

public class TerminalHandler {
	/**
	 * 
	 * @param commandSet
	 * @param responseString
	 */
	public static void checkOutputAndWaitForEnterKey(CommandSet commandSet, String responseString, Scanner scan){
		System.out.println("The command is " + commandSet.getCommand());
		System.out.println(responseString);
		System.out.println("Check the output and press enter to continue");

		scan.nextLine();
	}

	/**
	 * 
	 * @param commandSet
	 * @param responseString
	 * @return
	 */
	public static String inputYesOrNo(CommandSet commandSet, String responseString, Scanner scan){
		System.out.println("The command is " + commandSet.getCommand());
		System.out.println(responseString);
		System.out.print("Do you want to continue? [y/N]: ");

		String input = scan.nextLine();
		return input;
	}
}
