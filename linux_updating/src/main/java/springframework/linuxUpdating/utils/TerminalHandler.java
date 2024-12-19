package springframework.linuxupdating.utils;

import java.util.Scanner;

import org.springframework.stereotype.Component;

import springframework.linuxupdating.model.Command;
import springframework.linuxupdating.service.TerminalMsg;

@Component
public class TerminalHandler {
	private final TerminalMsg terminalMsg;

	public TerminalHandler(TerminalMsg terminalMsg){
		this.terminalMsg = terminalMsg;
	}

	public boolean checkOutputAndWaitForEnterKey(Command commandSet, String responseString, Scanner scan){
		System.out.println(terminalMsg.getTheCmdIsMsg() + commandSet.getCommand());
		System.out.println(responseString);
		System.out.println(terminalMsg.getCheckOutputMsg());

		String userInput =  scan.nextLine();
		if(userInput.equalsIgnoreCase("q")){
			System.out.println(terminalMsg.getSkipCmdMsg());
			scan.close();
			return true;
		}
		else{
			return false;
		}
	}

	public String inputYesOrNo(Command commandSet, String responseString, Scanner scan){
		System.out.println(terminalMsg.getTheCmdIsMsg() + commandSet.getCommand());
		System.out.println(responseString);

		String input = scan.nextLine();
		return input;
	}
}
