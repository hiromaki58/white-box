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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import springframework.linuxupdating.model.CommandSet;
import springframework.linuxupdating.utils.CommandList;
import springframework.linuxupdating.utils.TerminalHandler;

@Service
public class SshAccessor {
    @Value("${ssh.port}")
    private int port;

    @Value("${ssh.ubuntuUserName}")
    private String ubuntUserName;

    @Value("${ssh.redHatUserName}")
    private String redHatUserName;

    @Value("${ssh.privateKeyPath}")
    private String privateKeyPath;

    @Value("${ssh.publicKeyPath}")
    private String publicKeyPath;

    @Value("${ssh.pass}")
    private String pass;

    @Autowired
    private LogCreater logCreater;

    @Autowired
    private ConfigurableApplicationContext context;

	public SshAccessor() {
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

                sendCommandList(session, hostNameList, distributionList, i);
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
    private void sendCommandList(ClientSession session, List<String> hostNameList, List<String> distributionList, int numOfHost){
        List<CommandSet> commnadList;

        if(distributionList.get(numOfHost).equalsIgnoreCase("Ubuntu")){
            commnadList = CommandList.getUbuntuCommandList();
        }
        else{
            commnadList = CommandList.getRedHatCommandList();
        }

        for(CommandSet commandSet : commnadList){
            sendCommand(session, hostNameList, numOfHost, commandSet);
        }
    }

    private void sendCommand(ClientSession session, List<String> hostNameList, int numOfHost, CommandSet commandSet){
        String responseString;
        try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            ClientChannel channel = session.createExecChannel(commandSet.getCommand())) {

            channel.setOut(responseStream);
            channel.open().verify(5, TimeUnit.SECONDS);
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(60));

            responseString = new String(responseStream.toByteArray());

            if((responseString.contains("command not found") || responseString.contains("コマンドがありません")) && commandSet.getAlternativeCommand() != null){
                try(ByteArrayOutputStream alternativeResponseStream = new ByteArrayOutputStream();
                    ClientChannel alternativeChannel = session.createExecChannel(commandSet.getAlternativeCommand())){
                    alternativeChannel.setOut(alternativeResponseStream);
                    alternativeChannel.open().verify(5, TimeUnit.SECONDS);
                    alternativeChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(60));

                    responseString = new String(alternativeResponseStream.toByteArray());
                }
                catch(IOException e){
                    e.getStackTrace();
                }
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

            logCreater.saveLog(hostNameList.get(numOfHost), commandSet.getCommand());
            logCreater.saveLog(hostNameList.get(numOfHost), " ");
            logCreater.saveLog(hostNameList.get(numOfHost), responseString);

            if(!channel.isClosed()){
                channel.close();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
