package springframework.linuxupdating.utils;

import java.util.HashMap;
import java.util.Map;

import springframework.linuxupdating.utils.interfaces.UserNameProvider;

public class UserNameProviderFactory implements UserNameProvider{
    private final Map<String, String> userNameMap;

    public UserNameProviderFactory(){
        userNameMap = new HashMap<>();

        userNameMap.put("ubuntu", "ubuntu");
        userNameMap.put("redhat", "ec2-user");
    }

    @Override
    public String getUserName(String distribution) {
        return userNameMap.getOrDefault(distribution.toLowerCase(), "ubuntu");
    }
}
