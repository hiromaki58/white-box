package springframework.linuxupdating.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class Logger {
  private static final String LOG_DIR = "logs/";

  public static void saveLog(String hostName, String log){
    File logDir = new File(LOG_DIR);

    if(!logDir.exists()){
      logDir.mkdir();
    }

    try(BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_DIR+hostName+".txt"))){
      bw.write(log);
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }
}
