package springframework.linuxupdating.utils;

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
