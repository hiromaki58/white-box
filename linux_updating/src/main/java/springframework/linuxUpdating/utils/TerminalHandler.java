package springframework.linuxupdating.utils;

import java.util.Scanner;

import org.springframework.stereotype.Component;

import springframework.linuxupdating.model.CommandSet;
import springframework.linuxupdating.service.TerminalMsg;

@Component
public class TerminalHandler {
	private final TerminalMsg terminalMsg;

	public TerminalHandler(TerminalMsg terminalMsg){
		this.terminalMsg = terminalMsg;
	}

	public boolean checkOutputAndWaitForEnterKey(CommandSet commandSet, String responseString, Scanner scan){
		System.out.println("The command is " + commandSet.getCommand());
		System.out.println(responseString);
		// System.out.println("Check the output and press enter to continue, or type 'q' to skip or press enter to continue.");
		System.out.println(terminalMsg.getCheckOutputMsg());

		String userInput =  scan.nextLine();
		if(userInput.equalsIgnoreCase("q")){
			System.out.println("Skipping to the next command...");
			scan.close();
			return true;
		}
		else{
			return false;
		}
	}

	public static String inputYesOrNo(CommandSet commandSet, String responseString, Scanner scan){
		System.out.println("The command is " + commandSet.getCommand());
		System.out.println(responseString);

		String input = scan.nextLine();
		return input;
	}
}
