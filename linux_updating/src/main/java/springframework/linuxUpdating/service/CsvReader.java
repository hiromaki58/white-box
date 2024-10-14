package springframework.linuxupdating.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CsvReader {
    /**
     * 
     * @param filePath
     * @return
     */
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
                    ipAddrList.add(column[ipAddrPositionNum]);
                }
            }
        }
        catch(IOException e) {
            System.out.println("getIpAddrList error");
        }

        return ipAddrList;
    }

    /**
     * 
     * @param filePath
     * @return
     */
    public List<String> getHostNameList(String filePath) {
        final int csvColumnCount = 4;
        final int hostNamePositionNum = 1;
        List<String> hostNameList = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = br.readLine()) != null){
                String[] column = line.split(",");

                if(column.length > csvColumnCount - 1 && column[1].trim().isEmpty()){
                    System.out.println("Warning: Missing the 3rd value (Hostname)");
                }
                if(column.length > csvColumnCount - 1){
                    hostNameList.add(column[hostNamePositionNum]);
                }
            }
        }
        catch(IOException e){
            System.out.println("getHostNameList error");
        }

        return hostNameList;
    }
}
