package springframework.linuxupdating.utils;

import springframework.linuxupdating.utils.interfaces.CommandListProvider;

public class CommandListProviderFactory {
    public static CommandListProvider getCommandListProvider(String distribution){
        if(distribution.equalsIgnoreCase("Ubuntu")){
            return new UbuntuCommandListProvider();
        }
        else if(distribution.equalsIgnoreCase("RedHat")){
            return new RedHatCommandListProvider();
        }
        throw new IllegalArgumentException();
    }
}
