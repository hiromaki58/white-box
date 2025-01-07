# Introduction
Imagine you are keeping a single penguin as a pet — Let's call him Linux. As the caretaker, you need to feed him, clean his space, and take him to the vet if necessary. Managing one penguin might not be impossible　—　practicality aside.
But what if you had 20 penguins? Or 100?
To make matters even more complicated, penguins have subcategories, each requiring different living conditions and food preferences. What would you do if tasked with managing all of them regularly and consistently?
#### Target
With this somewhat silly prelude, here's the real issue: Over 100 Linux servers require monthly updates.
Instead of manually entering commands tailored to each distribution every time, I aimed to automate the process.
#### Approach
To reduce the burden on the operator, the solution runs with just jar, CSV, and a single command.
- Usernames and private keys for SSH connections are kept consistent.
- Paths to the necessary files for the following tasks remain fixed:
  - Private key
  - Public key
  - CSV file
  - Log directory
#### Steps
1, Read the CSV file
2, Establish SSH connections using public-key authentication
3, Execute commands tailored to each distribution
4, Write the results to logs
#### Environment
The target servers are Linux servers running on AWS EC2 instances.
To minimize development time, I used Java 1.8 and Spring Boot, as they were already installed on my workstation. However, given that it's Java 1.8, porting the solution to Python or C# would not be difficult.
# Reference
https://github.com/hiromaki58/white-box/tree/main/linux_updating
# Reading the CSV File
The list of servers requiring updates is created in CSV format. The reason for choosing CSV is that it allows easy creation in Excel, and any changes to the target servers can be made effortlessly.
CSV sample
```csv:CSV list example
CompanyA,AHostName,xxx.xxx.xxx.xxx,Ubuntu
CompanyB,BHostName,xxx.xxx.xxx.xxx,RedHat
```
Reading CSV
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
Divide CSV file for the next process
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
# SSH　access with public key cryptography
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
Read the key
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
# Executing commands based on distribution
The following are the steps for when the distribution is Ubuntu
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
#### Handling confirmation prompts during command execution
When executing commands, the server may prompt for confirmation. This section addresses how to handle those prompts.
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
# Writing execution results to logs
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
# Conclusion
Set up the trigger like below
```java:LinuxUpdatingApplication.java
@SpringBootApplication
public class LinuxUpdatingApplication implements CommandLineRunner{
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("No path is set.");
            return;
        }
        else {
            System.out.println("Success to have the csv file");
        }

        List<String> hostNameList = csvReader.getHostNameList(args[0]);
        List<String> ipAddrList = csvReader.getIpAddrList(args[0]);
        List<String> distributionList = csvReader.getDistributionList(args[0]);
    }
}
```
It should work with this command
```bash:command
java -jar LinuxUpdating.jar ./path/to/linux/server/list.csv
```
