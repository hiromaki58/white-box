package springframework.linuxupdating.utils;
import java.util.List;

import springframework.linuxupdating.model.Command;

public class RedHatCommandListProvider implements CommandListProvider{
    @Override
    public List<Command> getCommandList(){
        return CommandList.getRedHatCommandList();
    }
}
