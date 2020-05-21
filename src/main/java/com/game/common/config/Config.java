package com.game.common.config;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Config implements IConfig {

    private static Config applicationConfig;
    static {
        String property = System.getProperty("application.conf.path", "application.conf");
        com.typesafe.config.Config config = ConfigFactory.load(property);
        applicationConfig = new Config(config);
    }

    public static Config getInstance() {
        return applicationConfig;
    }

    private final com.typesafe.config.Config config;

    private Config(com.typesafe.config.Config config) {
        this.config = config;
    }

    @Override
    public IConfig getConfig(String path){
        return new Config(Objects.requireNonNull(config.getConfig(path)));
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
    public List<IConfig> getConfigList(String path){
        List<? extends com.typesafe.config.Config> configList = config.getConfigList(path);
        return configList.stream().map(Config::new).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path){
        ConfigList configList = config.getList(path);
        return (List<T>)configList.unwrapped();
    }

    @Override
    public long getDuration(String path, TimeUnit timeUnit) {
        return config.getDuration(path, timeUnit);
    }
}
