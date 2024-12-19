package springframework.linuxupdating.utils;

import java.util.List;

import springframework.linuxupdating.model.Command;

public class UbuntuCommandListProvider implements CommandListProvider{
  @Override
  public List<Command> getCommandList(){
    return CommandList.getUbuntuCommandList();
  }
}
