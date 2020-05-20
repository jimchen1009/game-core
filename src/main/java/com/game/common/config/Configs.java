package com.game.common.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Configs implements IConfigs{

    private static Configs configs;
    static {
        String property = System.getProperty("application.conf.path", "application.conf");
        Config config = ConfigFactory.load(property);
        configs = new Configs(config);
    }

    public static Configs getInstance() {
        return configs;
    }

    private final Config config;

    private Configs(Config config) {
        this.config = config;
    }

    @Override
    public IConfigs getConfig(String path){
        return new Configs(Objects.requireNonNull(config.getConfig(path)));
    }

    @Override
    public int getInt(String path){
        return config.getInt(path);
    }

    @Override
    public long getLong(String path){
        return config.getLong(path);
    }

    @Override
    public String getString(String path) {
        return config.getString(path);
    }

    @Override
    public List<IConfigs> getConfigList(String path){
        List<? extends Config> configList = config.getConfigList(path);
        return configList.stream().map(Configs::new).collect(Collectors.toList());
    }
}
