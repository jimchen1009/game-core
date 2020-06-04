package com.game.cache.mapper;

import com.game.common.config.Configs;
import com.game.common.config.IConfig;
import com.game.common.util.CommonUtil;

import java.util.List;

public class ClassConfig {

    public final String className;
    public final String tableName;
    public final int primarySharedId;
    public final boolean delayUpdate;
    public final boolean loadOnShared;

    private ClassConfig(String className, String tableName, int primarySharedId, boolean delayUpdate, boolean loadOnShared) {
        this.className = className;
        this.tableName = tableName;
        this.primarySharedId = primarySharedId;
        this.delayUpdate = delayUpdate;
        this.loadOnShared = loadOnShared;
    }

    public static ClassConfig loadConfig(Class<?> aClass){
        List<IConfig> configList = Configs.getInstance().getConfigList("cache.classes");
        return CommonUtil.applyOneIf( configList, config -> {
            String className = config.getString("className");
            if (className.equals(aClass.getName())){
                return null;
            }
            String tableName = config.getString("tableName");
            int primarySharedId = config.getInt("primarySharedId");
            boolean delayUpdate = config.getBoolean("delayUpdate");
            boolean loadOnShared = config.getBoolean("loadOnShared");
            return new ClassConfig(className, tableName, primarySharedId, delayUpdate, loadOnShared);
        });
    }
}
