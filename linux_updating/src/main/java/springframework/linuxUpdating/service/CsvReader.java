package springframework.linuxUpdating.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CsvReader {
  public List<String> getIpAddrList(String filePath) {
    final int csvColumnCount = 4;
    final int ipAddrPositionNum = 2;
    List<String> ipAddrList = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] column = line.split(",");

            if (column.length > csvColumnCount - 1 && column[2].trim().isEmpty()) {
                System.out.println("Warning: Missing the 3rd value (GIP)");
            }

            if (column.length > csvColumnCount - 1) {
                System.out.println("IP address is " + column[ipAddrPositionNum]);
                ipAddrList.add(column[ipAddrPositionNum]);
            }
        }
    }
    catch(IOException e) {
        System.out.println("getIpAddrList error");
    }

    return ipAddrList;
  }
}
