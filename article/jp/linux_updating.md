# はじめに
仮にあなたが1羽のペンギンを飼っていたとしましょう。- 彼の名前はLinux - とします。
あなたは飼い主として餌をあげ、トイレの掃除をし、必要があれば病院へ連れて行かなければなりません。
1羽であれば不可能ではないでしょう。 - 現実的かどうかはおいておいて - しかしこれが20羽であればどうでしょうか、あるいは100羽は？
されに言えばペンギンには下位のカテゴリーがあり、それぞれ住環境や好んで食べる食物の種類がことなります。
それらの管理を定期的に永続的に依頼されたらどうしますか？
#### やりたかったこと
くだらない御託を並べましたが、毎月アップデートをしなければいけないLinuxサーバーの数が100を超え、各ディストリビューションに合わせたコマンドを、毎回手入力する代わりに自動化します。
#### 方針
作業者への負担を減らすため、jarとCSVファイル・コマンド1行で動作させます。
そのためSSH接続に必要なユーザーネームと秘密鍵は同一にします。
また以下の作業に必要なファイルへのパスは常に同じとします。
- 秘密鍵
- 公開鍵
- CSVファイル
- ログ格納ディレクトリ
#### コードの動き
1, CSV読み込み
2, 公開鍵方式によるSSH接続
3, ディストリビューションに合わせたコマンドの実行
4, 実行結果をログに書き出す
#### 動作環境
AWS EC2インスタンスにあるLinuxサーバーを対象にしています。
製造工数を減らたかったため、すでに作業PCにインストールされていたJava 1.8/spring bootを使っています。しかし1.8ですのでPythonやC#への移植も難しくないと思います。
# Reference
https://github.com/hiromaki58/white-box/tree/main/linux_updating
# CSV読み込み
アップデートが必要なサーバーリストをCSVの形で作成します。CSVを入力に選択した理由ですが、Excelでの作成が可能で作業対象サーバーが変更になっても修正が簡単です。
CSVサンプル
```csv:CSVリスト例
CompanyA,AHostName,xxx.xxx.xxx.xxx,Ubuntu
CompanyB,BHostName,xxx.xxx.xxx.xxx,RedHat
```
CSVを読むコード
```java:CsvReader.java
private List<String> getColumnInfoList(String filePath, int columnIndex, String columnTitle){
    final int csvColumnCount = 4;
    List<String> inputList = new ArrayList<>();

    try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
        String line;
        while((line = br.readLine()) != null){
            String[] columnList = line.split(",");

            if(columnList.length > csvColumnCount - 1 && columnList[columnIndex].trim().isEmpty()){
                System.err.println("Warning : Missing the value in column " + columnTitle);
            }

            if(columnList.length > csvColumnCount - 1){
                inputList.add(columnList[columnIndex]);
            }
        }
    }
    catch(IOException e){
        System.err.println("Fail to read the file for " + columnTitle);
    }
    return inputList;
}
```
作業がしやすいようCSVを分割
```java:CsvReader.java
public List<String> getHostNameList(String filePath) {
    final int hostNamePositionNum = 1;
    final String columnTitle = "Hostname";
    return getColumnInfoList(filePath, hostNamePositionNum, columnTitle);
}

public List<String> getIpAddrList(String filePath) {
    final int ipAddrPositionNum = 2;
    final String columnTitle = "IpAddress";
    return getColumnInfoList(filePath, ipAddrPositionNum, columnTitle);
}

public List<String> getDistributionList(String filePath){
    final int distributionPositionNum = 3;
    final String columnTitle = "Distribution";
    return getColumnInfoList(filePath, distributionPositionNum, columnTitle);
}
```
# 公開鍵方式によるSSH接続
分割したリストに基づいてそれぞれのサーバーへSSH接続
```java:SshAccessor.java
public void connect(List<String> hostNameList, List<String> ipAddrList, List<String> distributionList) {
    SshClient client = SshClient.setUpDefaultClient();
    client.start();

    for(int i = 0; i < hostNameList.size(); i++){
        String userName= userNameProviderFactory.getUserName(distributionList.get(i));

        try (ClientSession session = client.connect(userName, ipAddrList.get(i), port).verify(10000).getSession()) {
            char[] passphrase = pass.toCharArray();
            KeyPair keyPair = SshKeyCreater.loadKeyPair(privateKeyPath, publicKeyPath, passphrase);
            session.addPublicKeyIdentity(keyPair);

            session.auth().verify(5000);

            sendCommandList(session, hostNameList.get(i), distributionList.get(i));
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    client.stop();
    int exitCode = SpringApplication.exit(context, () -> 0);
    System.exit(exitCode);
}
```
鍵の読み込み
```java:SshKeyCreater.java
public static KeyPair loadKeyPair(String privateKeyPath, String publicKeyPath, char[] passphrase) throws Exception {
    // Read the secret key
    PEMParser pemParser = new PEMParser(new FileReader(privateKeyPath));
    Object object = pemParser.readObject();
    pemParser.close();

    JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    PrivateKey privateKey;

    // Decode the secret key with passphrase
    if (object instanceof PEMEncryptedKeyPair) {
        PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) object;
        PEMKeyPair pemKeyPair = encryptedKeyPair.decryptKeyPair(new JcePEMDecryptorProviderBuilder().build(passphrase));
        privateKey = converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
    }
    // Without passphrase
    else if (object instanceof PEMKeyPair) {
        privateKey = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
    }
    else {
        throw new IllegalArgumentException("Invalid private key format.");
    }

    // Read the public key
    PublicKey publicKey = loadOpenSSHPublicKey(publicKeyPath);

    return new KeyPair(publicKey, privateKey);
}
```
# ディストリビューションに合わせたコマンドの実行
以下はディストリビューションがUbuntuだった場合のステップ
```java:SshAccessor.java
private void sendCommandList(ClientSession session, String hostName, String distribution){
    List<Command> commnadList;
    commnadList = CommandListProviderFactory.getCommandListProvider(distribution).getCommandList();

    for(Command commandSet : commnadList){
        sendCommand(session, hostName, commandSet);
    }
}
```
```java:CommandListProviderFactory.java
public static CommandListProvider getCommandListProvider(String distribution){
    if(distribution.equalsIgnoreCase("Ubuntu")){
        return new UbuntuCommandListProvider();
    }
    else if(distribution.equalsIgnoreCase("RedHat")){
        return new RedHatCommandListProvider();
    }
    throw new IllegalArgumentException();
}
```
```java:UbuntuCommandListProvider.java
public class UbuntuCommandListProvider implements CommandListProvider{
    @Override
    public List<Command> getCommandList(){
        return CommandList.getUbuntuCommandList();
    }
}
```
```java:CommandList.java
public static List<Command> getUbuntuCommandList(){
    List<Command> commandList = new ArrayList<>();
    commandList.add(new Command("hostname", null, true, false));
    commandList.add(new Command("cat /etc/issue", null, true, false));
    commandList.add(new Command("sudo apt update", null, false, false));
    commandList.add(new Command("sudo apt upgrade", null, false, true));
    commandList.add(new Command("ps aux | grep apache2 | grep -v grep", null, true, false));

    return commandList;
}
```
#### コマンド実行時の確認対応
コマンドを実行する時にサーバーから確認されることがあります。それへの対応です。
```java:SscAccessor.java
private void sendCommand(ClientSession session, String hostName, Command commandSet){
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
            if(terminalHandler.checkOutputAndWaitForEnterKey(commandSet, responseString, scan)){
                return;
            };
        }

        if(commandSet.isAskedToSayYesOrNo()){
            Scanner scan = new Scanner(System.in);
            String userInput = terminalHandler.inputYesOrNo(commandSet, responseString, scan);
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
```
# 実行結果をログに書き出す
```java:LogCreater.java
public void saveLog(String hostName, String logContent){
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    String date = dateFormat.format(new Date());
    String logFileTitle = logStoragePath + File.separator + date + "-" + hostName + ".txt";

    try(BufferedWriter bw = new BufferedWriter(new FileWriter(logFileTitle, true))){
        bw.write(logContent);
    }
    catch(IOException e){
        System.err.println("Fail to save the log for the host name is " + hostName);
        e.printStackTrace();
    }
}
```
