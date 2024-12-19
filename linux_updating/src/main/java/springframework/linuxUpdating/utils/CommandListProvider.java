package springframework.linuxupdating.utils;

import java.util.List;

import springframework.linuxupdating.model.Command;

public interface CommandListProvider {
    List<Command> getCommandList();
}
