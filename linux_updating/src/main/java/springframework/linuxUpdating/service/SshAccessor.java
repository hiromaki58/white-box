package springframework.linuxupdating.service;

import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
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

@Service
public class SshAccessor {
    @Value("${ssh.port}")
    private int port;

    @Value("${ssh.username}")
    private String username;

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

	public void connect(List<String> ipAddrList, List<String>hostNameList) {
		String responseString = "";
		String ipAddr = ipAddrList.get(0);
        String hostName = hostNameList.get(0);
        System.out.println("host name is " + hostName);
		SshClient client = SshClient.setUpDefaultClient();
		client.start();

		try (ClientSession session = client.connect(username, ipAddr, port).verify(10000).getSession()) {
			// Read private and public keies
			char[] passphrase = pass.toCharArray();
			KeyPair keyPair = SshKeyCreater.loadKeyPair(privateKeyPath, publicKeyPath, passphrase);
			session.addPublicKeyIdentity(keyPair);

			// Authorization
			session.auth().verify(5000);

            List<CommandSet> commnadList = getCommandList();

			// Send linux command
            for(CommandSet commandSet : commnadList){
                try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                    ClientChannel channel = session.createExecChannel(commandSet.getCommand())) {

                    channel.setOut(responseStream);
                    channel.open().verify(5, TimeUnit.SECONDS);
                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(60));

                    responseString = new String(responseStream.toByteArray());

                    // Show the response in the terminal
                    if(commandSet.getIsShownInTerminal()){
                        System.out.println(responseString);
                    }

                    channel.close();

                    logCreater.saveLog(hostName, responseString);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            session.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			client.stop();
		}

        // Shut down the applicatoin when command is completed.
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
	}

    /**
     * Make up the command list
     * @return command list
     */
	private List<CommandSet> getCommandList(){
        List<CommandSet> commandList = new ArrayList<>();
        commandList.add(new CommandSet("hostname", false));
        commandList.add(new CommandSet("ps aux | grep apache2 | grep -v grep", true));

        return commandList;
	}
}
