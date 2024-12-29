package springframework.linuxUpdating.service;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import springframework.linuxupdating.service.LogCreater;

@SpringBootTest
public class LogCreaterTest {
    @Autowired
    private LogCreater logCreater;

    @Test
    public void testSaveLog() throws IOException {
        // テスト用データ
        String hostName = "test-host";
        String logContent = "This is a test log content.";

        // メソッド実行
        logCreater.saveLog(hostName, logContent);
        System.out.println("Testing saveLog method is done");

        // String expectedFileName = logStoragePath + File.separator + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + hostName + ".txt";
        // File logFile = new File(expectedFileName);

        // assertTrue(logFile.exists(), "Log file should be created.");
        // assertEquals(logContent, Files.readString(logFile.toPath()), "Log content should match.");
    }
}
