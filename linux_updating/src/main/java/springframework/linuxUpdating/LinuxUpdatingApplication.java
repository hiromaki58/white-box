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
        // This part is for calling the jar file
        // if (args.length == 0) {
        //   System.out.println("No path is set.");
        //   return;
        // }
        // else {
        //   System.out.println("Success to have the csv file");
        // }

        // List<String> hostNameList = csvReader.getHostNameList(args[0]);
        // List<String> ipAddrList = csvReader.getIpAddrList(args[0]);
        // List<String> distributionList = csvReader.getDistributionList(args[0]);

        // This part is for checking the function in IDE
        // Set the path to the csv file
        String csvFile = "src/test/java/springframework/linuxUpdating/mockInpu?t.csv";
        List<String> hostNameList = csvReader.getHostNameList(csvFile);
        List<String> ipAddrList = csvReader.getIpAddrList(csvFile);
        List<String> distributionList = csvReader.getDistributionList(csvFile);

        sshAccessor.connect(hostNameList, ipAddrList, distributionList);
    }

    public static void main(String[] args) {
        SpringApplication.run(LinuxUpdatingApplication.class, args);
    }
}
