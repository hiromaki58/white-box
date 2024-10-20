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
     * @param columnIndex
     * @param columnTitle
     * @return
     */
    private List<String> getColumnInfoList(String filePath, int columnIndex, String columnTitle){
        final int csvColumnCount = 4;
        List<String> inputList = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = br.readLine()) != null){
                String[] columnList = line.split(",");

                if(columnList.length > csvColumnCount - 1 && columnList[columnIndex].trim().isEmpty()){
                    System.err.println("Warning : Missing the value in column " + columnTitle);
                }

                if(columnList.length > csvColumnCount - 1){
                    inputList.add(columnList[columnIndex]);
                }
            }
        }
        catch(IOException e){
            System.err.println("Fail to read the file for " + columnTitle);
        }

        return inputList;
    }

    /**
     * 
     * @param filePath
     * @return
     */
    public List<String> getHostNameList(String filePath) {
        final int hostNamePositionNum = 1;
        final String columnTitle = "Hostname";
        return getColumnInfoList(filePath, hostNamePositionNum, columnTitle);
    }

    /**
     * 
     * @param filePath
     * @return
     */
    public List<String> getIpAddrList(String filePath) {
        final int ipAddrPositionNum = 2;
        final String columnTitle = "IpAddress";
        return getColumnInfoList(filePath, ipAddrPositionNum, columnTitle);
    }

    /**
     * 
     * @param filePath
     * @return
     */
    public List<String> getDistributionList(String filePath){
        final int distributionPositionNum = 3;
        final String columnTitle = "Distribution";
        return getColumnInfoList(filePath, distributionPositionNum, columnTitle);
    }
}
