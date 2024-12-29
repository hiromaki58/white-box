package springframework.linuxupdating.utils.interfaces;
import java.util.List;

import springframework.linuxupdating.model.Command;

public interface CommandListProvider {
    List<Command> getCommandList();
}
