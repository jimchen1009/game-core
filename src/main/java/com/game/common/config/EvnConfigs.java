package com.game.common.config;

import com.typesafe.config.ConfigList;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EvnConfigs implements IEvnConfig {

    private final com.typesafe.config.Config config;

    EvnConfigs(com.typesafe.config.Config config) {
        this.config = config;
    }

    @Override
    public boolean hasPath(String path) {
        return config.hasPath(path);
    }

    @Override
    public IEvnConfig getConfig(String path){
        return config.hasPath(path) ? new EvnConfigs(config.getConfig(path)) : null;
    }

    @Override
    public int getInt(String path){
        return config.hasPath(path) ? config.getInt(path) : 0;
    }

    @Override
    public long getLong(String path){
        return config.hasPath(path) ? config.getLong(path) : 0L;
    }

    @Override
    public String getString(String path) {
        return config.hasPath(path) ? config.getString(path) : null;
    }

    @Override
    public boolean getBoolean(String path) {
        return config.hasPath(path) && config.getBoolean(path);
    }

    @Override
    public List<IEvnConfig> getConfigList(String path){
        if (!config.hasPath(path)) {
            return null;
        }
        List<? extends com.typesafe.config.Config> configList = config.getConfigList(path);
        return configList.stream().map(EvnConfigs::new).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path){
        if (!config.hasPath(path)) {
            return null;
        }
        ConfigList configList = config.getList(path);
        return (List<T>)configList.unwrapped();
    }

    @Override
    public long getDuration(String path, TimeUnit timeUnit) {
        return config.getDuration(path, timeUnit);
    }
}
