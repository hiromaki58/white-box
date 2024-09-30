package springframework.linuxupdating.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogCreater {
    @Value("${logCreater.logStoragePath}")
    private String logStoragePath;

    public LogCreater(){
    }

    @PostConstruct
    public void init() {
        initializeLogDirectory();
    }

    /**
     * Save the response from the command
     * @param ipAddr Target server IP address
     * @param logContent Command response
     */
    public void saveLog(String ipAddr, String logContent){
        String logFileTitle = logStoragePath + File.separator + ipAddr.replace(".", "_") + "_log.txt";

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(logFileTitle, true))){
            bw.write(logContent);
            bw.newLine();
            System.out.println("Save the log file of IP address is " + ipAddr);
        }
        catch(IOException e){
            System.err.println("Fail to save the log for the IP address is " + ipAddr);
            e.printStackTrace();
        }
    }

    /**
     * Set up the log strage directory path
     */
    private void initializeLogDirectory(){
        try{
            Files.createDirectories(Paths.get(logStoragePath));
        }
        catch(IOException e){
            System.err.println("Fail to create the log directory");
            e.printStackTrace();
        }
    }
}
