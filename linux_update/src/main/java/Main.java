package main.java;

import java.util.List;

public class Main {
    public static void main(String[] args){
      String csvFile = "src/test/java/mockInput.csv";

      List<String> ipAddrList = CsvReader.getIpAddrList(csvFile);
      System.out.println("Get ip addr list");
    }
}