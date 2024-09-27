package springframework.linuxupdating;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springframework.linuxupdating.service.CsvReader;
import springframework.linuxupdating.service.SshAccessor;

@SpringBootApplication
public class LinuxUpdatingApplication implements CommandLineRunner{
  @Autowired
  private CsvReader csvReader;

  @Autowired
  private SshAccessor sshAccessor;

  @Override
  public void run(String... args) throws Exception {
    // if (args.length == 0) {
    //   System.out.println("No path is set.");
    //   return;
    // }
    // else {
    //   System.out.println("Success to have the csv file");
    // }

      String csvFile = "src/test/java/springframework/linuxUpdating/mockInput.csv"; // 実際のCSVファイルパスを指定
      // List<String> ipAddrList = csvReader.getIpAddrList(args[0]);
      List<String> ipAddrList = csvReader.getIpAddrList(csvFile);
      System.out.println("Get IP address list");

      sshAccessor.connect();
      // sshAccessor.disconnect();
    }

    public static void main(String[] args) {
      SpringApplication.run(LinuxUpdatingApplication.class, args);
    }
}
