package springframework.linuxupdating.service;

import java.util.List;
import java.util.Scanner;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import springframework.linuxupdating.model.CommandSet;
import springframework.linuxupdating.utils.CommandList;
import springframework.linuxupdating.utils.TerminalHandler;

@Service
public class SshAccessor {
    private int port;
    private final String ubuntUserName;
    private final String redHatUserName;
    private final String privateKeyPath;
    private final String publicKeyPath;
    private final String pass;

    private final LogCreater logCreater;
    private final ConfigurableApplicationContext context;

	public SshAccessor(
        @Value("${ssh.port}") int port,
        @Value("${ssh.ubuntuUserName}") String ubuntuUserName,
        @Value("${ssh.redHatUserName}") String redHatUserName,
        @Value("${ssh.privateKeyPath}") String privateKeyPath,
        @Value("${ssh.publicKeyPath}") String publicKeyPath,
        @Value("${ssh.pass}") String pass,
        LogCreater logCreater,
        ConfigurableApplicationContext context
    )
    {
        this.port = port;
        this.ubuntUserName = ubuntuUserName;
        this.redHatUserName = redHatUserName;
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
        this.pass = pass;
        this. logCreater = logCreater;
        this.context = context;
	}

    /**
     * Access to the AWS EC2 instance
     * @param hostNameList
     * @param ipAddrList
     * @param distributionList
     */
	public void connect(List<String> hostNameList, List<String> ipAddrList, List<String> distributionList) {
		SshClient client = SshClient.setUpDefaultClient();
		client.start();

        for(int i = 0; i < hostNameList.size(); i++){
            String userName= "";
            if(distributionList.get(i).equalsIgnoreCase("ubuntu")){
                userName = ubuntUserName;
            }
            else {
                userName = redHatUserName;
            }
            try (ClientSession session = client.connect(userName, ipAddrList.get(i), port).verify(10000).getSession()) {
                // Read private and public keies
                char[] passphrase = pass.toCharArray();
                KeyPair keyPair = SshKeyCreater.loadKeyPair(privateKeyPath, publicKeyPath, passphrase);
                session.addPublicKeyIdentity(keyPair);

                // Authorization
                session.auth().verify(5000);

                sendCommandList(session, hostNameList.get(i), distributionList.get(i));
                session.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        client.stop();
        // Shut down the applicatoin when command is completed.
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
	}

    /**
     * Handle bunch of hosts
     * @param session
     * @param hostNameList
     * @param distributionList
     * @param numOfHost
     */
    private void sendCommandList(ClientSession session, String hostName, String distribution){
        List<CommandSet> commnadList;

        if(distribution.equalsIgnoreCase("Ubuntu")){
            commnadList = CommandList.getUbuntuCommandList();
        }
        else{
            commnadList = CommandList.getRedHatCommandList();
        }

        for(CommandSet commandSet : commnadList){
            sendCommand(session, hostName, commandSet);
        }
    }

    /**
     * Send the commands
     * @param session
     * @param hostNameList
     * @param numOfHost
     * @param commandSet
     */
    private void sendCommand(ClientSession session, String hostName, CommandSet commandSet){
        String responseString;
        try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            ClientChannel channel = session.createExecChannel(commandSet.getCommand())) {

            channel.setOut(responseStream);
            channel.open().verify(5, TimeUnit.SECONDS);
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(60));

            responseString = new String(responseStream.toByteArray());

            // In case of the commmand does not have any response
            if((responseString.contains("command not found") || responseString.contains("コマンドがありません")) && commandSet.getAlternativeCommand() != null){
                sendCommand(session, hostName, commandSet);
                return;
            }

            // Show the response in the terminal and aks to keep going or not
            if(commandSet.getIsContinuedOrNo()){
                Scanner scan = new Scanner(System.in);
                if(TerminalHandler.checkOutputAndWaitForEnterKey(commandSet, responseString, scan)){
                    return;
                };
            }

            if(commandSet.isAskedToSayYesOrNo()){
                Scanner scan = new Scanner(System.in);
                String userInput = TerminalHandler.inputYesOrNo(commandSet, responseString, scan);
                OutputStream out = channel.getInvertedIn();

                out.write((userInput + "\\n").getBytes());
                out.flush();
            }

            logCreater.saveLog(hostName, commandSet.getCommand());
            logCreater.saveLog(hostName, " ");
            logCreater.saveLog(hostName, responseString);

            if(!channel.isClosed()){
                channel.close();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
