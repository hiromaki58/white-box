package springframework.linuxupdating.utils;

import java.util.ArrayList;
import java.util.List;
import springframework.linuxupdating.model.CommandSet;

public class CommandList {
    public static List<CommandSet> getUbuntuCommandList(){
        List<CommandSet> commandList = new ArrayList<>();
        commandList.add(new CommandSet("hostname", true, false));
        commandList.add(new CommandSet("cat /etc/issue", true, false));
        commandList.add(new CommandSet("sudo apt update", false, false));
        commandList.add(new CommandSet("sudo apt upgrade", false, true));
        commandList.add(new CommandSet("ps aux | grep apache2 | grep -v grep", true, false));

        return commandList;
	}

    public static List<CommandSet> getRedHatCommandList(){
        List<CommandSet> commandList = new ArrayList<>();
        commandList.add(new CommandSet("hostname", true, false));
        commandList.add(new CommandSet("cat /etc/redhat-release", true, false));
        commandList.add(new CommandSet("ps -afw | grep apache2 | grep -v grep", false, false));
        commandList.add(new CommandSet("netstat -apnt | grep apache2 | grep -v grep", false, false));
        commandList.add(new CommandSet("yum check-update", false, false));
        commandList.add(new CommandSet("yum update", false, true));

        commandList.add(new CommandSet("ps -afw | grep apache2 | grep -v grep", false, false));
        commandList.add(new CommandSet("netstat -apnt | grep apache2 | grep -v grep", false, false));
        commandList.add(new CommandSet("yum check-update", false, false));

        return commandList;
    }
}
