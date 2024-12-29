package springframework.linuxupdating.utils;

import java.util.ArrayList;
import java.util.List;
import springframework.linuxupdating.model.Command;

public class CommandList {
    public static List<Command> getUbuntuCommandList(){
        List<Command> commandList = new ArrayList<>();
        commandList.add(new Command("hostname", null, true, false));
        commandList.add(new Command("cat /etc/issue", null, true, false));
        commandList.add(new Command("sudo apt update", null, false, false));
        commandList.add(new Command("sudo apt upgrade", null, false, true));
        commandList.add(new Command("ps aux | grep apache2 | grep -v grep", null, true, false));

        return commandList;
	}

    public static List<Command> getRedHatCommandList(){
        List<Command> commandList = new ArrayList<>();
        commandList.add(new Command("hostname", null, true, false));
        commandList.add(new Command("cat /etc/redhat-release", null, true, false));
        commandList.add(new Command("ps -afw | grep apache2 | grep -v grep", null, false, false));
        commandList.add(new Command("netstat -apnt | grep \"LISTENING\" | grep -v grep", "ss -tuln", false, false));
        commandList.add(new Command("sudo yum check-update", null, false, false));
        commandList.add(new Command("sudo yum update", null, false, true));

        commandList.add(new Command("ps -afw | grep apache2 | grep -v grep", null, false, false));
        commandList.add(new Command("netstat -apnt | grep \"LISTENING\" | grep -v grep", "ss -tuln", false, false));
        commandList.add(new Command("yum check-update", null, false, false));

        return commandList;
    }
}
