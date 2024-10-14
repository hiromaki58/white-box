package springframework.linuxupdating.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    public void saveLog(String hostName, String logContent){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = dateFormat.format(new Date());
        String logFileTitle = logStoragePath + File.separator + date + "-" + hostName + ".txt";

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(logFileTitle, true))){
            bw.write(logContent);
            bw.newLine();
        }
        catch(IOException e){
            System.err.println("Fail to save the log for the host name is " + hostName);
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
