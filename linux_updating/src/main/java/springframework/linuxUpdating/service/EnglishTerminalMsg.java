package springframework.linuxupdating.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("EN")
public class EnglishTerminalMsg implements TerminalMsg{
    @Value("${terminal.msg.checkOutputMsg}")
    private String checkOutputMsg;

    @Value("${terminal.msg.theCmdIsMsg}")
    private String theCmdIsMsg;

    @Value("${terminal.mgs.skipCmdMsg}")
    private String skipCmdMsg;

    @Override
    public String getCheckOutputMsg() {
        return checkOutputMsg;
    }

    @Override
    public String getTheCmdIsMsg(){
        return theCmdIsMsg;
    }

    @Override
    public String getSkipCmdMsg() {
        return skipCmdMsg;
    }
}
