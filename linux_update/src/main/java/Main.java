package main.java;

import java.util.List;

public class Main {
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("No path is set.");
			return;
		}

		String csvFile = "src/test/java/mockInput.csv";

		List<String> ipAddrList = CsvReader.getIpAddrList(csvFile);
		System.out.println("Get ip addr list");
	}
}