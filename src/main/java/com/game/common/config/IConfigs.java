package com.game.common.config;

import java.util.List;

public interface IConfigs {

    IConfigs getConfig(String path);

    int getInt(String path);

    long getLong(String path);

    String getString(String path);

    List<IConfigs> getConfigList(String path);
}
