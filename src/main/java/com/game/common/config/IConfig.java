package com.game.common.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface IConfig {


    boolean hasPath(String path);

    IConfig getConfig(String path);

    int getInt(String path);

    long getLong(String path);

    String getString(String path);

    boolean getBoolean(String path);

    List<IConfig> getConfigList(String path);

    <T> List<T> getList(String path);

    long getDuration(String path, TimeUnit timeUnit);
}
