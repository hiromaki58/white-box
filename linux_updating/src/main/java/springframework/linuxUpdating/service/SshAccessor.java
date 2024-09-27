package springframework.linuxupdating.service;

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
    @Value("${ssh.host}")
    private String host;

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

	public void connect() {
		SshClient client = SshClient.setUpDefaultClient();
		client.start();

		try (ClientSession session = client.connect(username, host, port).verify(10000).getSession()) {
			// 公開鍵と秘密鍵の読み込み
			char[] passphrase = pass.toCharArray();
			KeyPair keyPair = SSHKeyLoader.loadKeyPair(privateKeyPath, publicKeyPath, passphrase);
			session.addPublicKeyIdentity(keyPair);

			// 認証の実行
			session.auth().verify(5000);

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
