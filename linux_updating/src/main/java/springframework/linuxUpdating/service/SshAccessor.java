package springframework.linuxupdating.service;

import java.util.List;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

	public SshAccessor() {
	}

	public void connect(List<String> hostList) {
		String host = hostList.get(0);
		SshClient client = SshClient.setUpDefaultClient();
		client.start();

		try (ClientSession session = client.connect(username, host, port).verify(10000).getSession()) {
			// Read private and public keies
			char[] passphrase = pass.toCharArray();
			KeyPair keyPair = SshKeyCreater.loadKeyPair(privateKeyPath, publicKeyPath, passphrase);
			session.addPublicKeyIdentity(keyPair);

			// Authorization
			session.auth().verify(5000);

            // Senc linux command
			try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
				ClientChannel channel = session.createExecChannel("hostname")) {

				channel.setOut(responseStream);
				channel.open().verify(5, TimeUnit.SECONDS);
				channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(5));

				String responseString = new String(responseStream.toByteArray());
				System.out.println("Command output: " + responseString);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			client.stop();
		}
	}
}
