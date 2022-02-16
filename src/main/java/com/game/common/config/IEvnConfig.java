package com.game.common.config;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public interface IEvnConfig {

    boolean hasPath(String path);

    IEvnConfig getConfig(String path);

    int getInt(String path);

    long getLong(String path);

    String getString(String path);

    boolean getBoolean(String path);

    List<IEvnConfig> getConfigList(String path);

    <T> List<T> getList(String path);

    long getDuration(String path, TimeUnit timeUnit);

    Properties toProperties();
}
