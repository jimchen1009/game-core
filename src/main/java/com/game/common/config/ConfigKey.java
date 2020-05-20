package com.game.common.config;

public class ConfigKey {

    public static final ConfigKey Cache = new ConfigKey("cache");

    private final String keyName;

    private ConfigKey(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public String createKeyName(String pathName){
        return String.format("%s.%s", keyName, pathName);
    }

    public ConfigKey createConfigKey(String pathName){
        return new ConfigKey(createKeyName(pathName));
    }
}
