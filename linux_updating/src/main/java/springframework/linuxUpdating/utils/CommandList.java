package springframework.linuxupdating.utils;

import java.util.ArrayList;
import java.util.List;
import springframework.linuxupdating.model.CommandSet;

public class CommandList {
    public static List<CommandSet> getUbuntuCommandList(){
        List<CommandSet> commandList = new ArrayList<>();
        commandList.add(new CommandSet("hostname", null, true, false));
        commandList.add(new CommandSet("cat /etc/issue", null, true, false));
        commandList.add(new CommandSet("sudo apt update", null, false, false));
        commandList.add(new CommandSet("sudo apt upgrade", null, false, true));
        commandList.add(new CommandSet("ps aux | grep apache2 | grep -v grep", null, true, false));

        return commandList;
	}

    public static List<CommandSet> getRedHatCommandList(){
        List<CommandSet> commandList = new ArrayList<>();
        commandList.add(new CommandSet("hostname", null, true, false));
        commandList.add(new CommandSet("cat /etc/redhat-release", null, true, false));
        commandList.add(new CommandSet("ps -afw | grep apache2 | grep -v grep", null, false, false));
        commandList.add(new CommandSet("netstat -apnt | grep \"LISTENING\" | grep -v grep", "ss -tuln", false, false));
        commandList.add(new CommandSet("sudo yum check-update", null, false, false));
        commandList.add(new CommandSet("sudo yum update", null, false, true));

        commandList.add(new CommandSet("ps -afw | grep apache2 | grep -v grep", null, false, false));
        commandList.add(new CommandSet("netstat -apnt | grep \"LISTENING\" | grep -v grep", "ss -tuln", false, false));
        commandList.add(new CommandSet("yum check-update", null, false, false));

        return commandList;
    }
}
