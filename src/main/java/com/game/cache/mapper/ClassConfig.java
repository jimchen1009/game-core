package com.game.cache.mapper;

import com.game.common.config.Configs;
import com.game.common.config.IConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassConfig {

    private static final String NO_DB = "nodb";

    public final String className;
    public final String tableName;
    public final boolean enableRedis;
    public final int primarySharedId;
    public final boolean delayUpdate;
    public final boolean loadOnShared;
    public final int versionId;

    private final String redisKeyString;

    private ClassConfig(String className, String tableName, boolean enableRedis, int primarySharedId, boolean delayUpdate, boolean loadOnShared, int versionId) {
        this.className = className;
        this.tableName = tableName;
        this.enableRedis = enableRedis;
        this.primarySharedId = primarySharedId;
        this.delayUpdate = delayUpdate;
        this.loadOnShared = loadOnShared;
        this.versionId = versionId;
        this.redisKeyString = "%s_" + String.format("%s%s.v%s", tableName ,primarySharedId, versionId);
    }

    public int getPrimarySharedId() {
        return primarySharedId;
    }

    public String getRedisKeyString(String primaryKey) {
        return String.format(redisKeyString, primaryKey);
    }

    public boolean isNoDbCache(){
        return tableName.equals(NO_DB);
    }

    @Override
    public String toString() {
        return "{" +
                "className='" + className + '\'' +
                ", tableName='" + tableName + '\'' +
                ", enableRedis=" + enableRedis +
                ", primarySharedId=" + primarySharedId +
                ", delayUpdate=" + delayUpdate +
                ", loadOnShared=" + loadOnShared +
                ", versionId=" + versionId +
                '}';
    }

    private static final Map<String, List<ClassConfig>> name2ConfigList = new HashMap<>();
    private static final Map<String, ClassConfig> class2Configs = new HashMap<>();
    private static final Map<String, List<Integer>> class2SharedIdList = new HashMap<>();
    static {
        List<IConfig> configList = Configs.getInstance().getConfigList("cache.classes");
        for (IConfig config : configList) {
            String tableName = config.getString("tableName");
            boolean enableRedis = config.getBoolean("enableRedis");
            List<IConfig> sharedClassesConfigList = config.getConfigList("sharedClasses");
            for (IConfig classesConfig : sharedClassesConfigList) {
                String className = classesConfig.getString("className");
                int primarySharedId = classesConfig.getInt("primarySharedId");
                boolean delayUpdate = classesConfig.getBoolean("delayUpdate");
                boolean loadOnShared = classesConfig.getBoolean("loadOnShared");
                int versionId = classesConfig.getInt("versionId");
                ClassConfig classConfig = new ClassConfig(className, tableName, enableRedis, primarySharedId, delayUpdate, loadOnShared, versionId);
                class2Configs.put(classConfig.className, classConfig);
                List<ClassConfig> classConfigs = name2ConfigList.computeIfAbsent(classConfig.tableName, key -> new ArrayList<>());
                classConfigs.add(classConfig);
            }
        }
        for (Map.Entry<String, List<ClassConfig>> entry : name2ConfigList.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt(ClassConfig::getPrimarySharedId));
            List<Integer> sharedIdList = entry.getValue().stream()
                    .map(ClassConfig::getPrimarySharedId).collect(Collectors.toList());
            if (sharedIdList.size() < 2){
                continue;
            }
            if (new HashSet<>(sharedIdList).size() < sharedIdList.size()) {
                throw new IllegalArgumentException(entry.getValue() + "");
            }
            if (sharedIdList.contains(0)){
                throw new IllegalArgumentException(entry.getValue() + "");
            }
            sharedIdList = sharedIdList.stream().filter(sharedId -> sharedId > 0).collect(Collectors.toList());
            class2SharedIdList.put(entry.getKey(), sharedIdList);
        }
    }

    public static ClassConfig getConfig(Class<?> aClass){
        return class2Configs.get(aClass.getName());
    }

    public static List<Integer> getPrimarySharedIdList(String className){
        List<Integer> sharedIdList = class2SharedIdList.get(className);
        return sharedIdList == null ? new ArrayList<>(0) : new ArrayList<>(sharedIdList);
    }

    public static List<ClassConfig> getPrimarySharedConfigList(String tableName){
        List<ClassConfig> classConfigs = name2ConfigList.get(tableName);
        return classConfigs == null ? new ArrayList<>(0) : new ArrayList<>(classConfigs);
    }

    public static ClassConfig getConfig(String tableName, int primarySharedId){
        List<ClassConfig> classConfigs = name2ConfigList.get(tableName);
        Optional<ClassConfig> optional = classConfigs.stream().filter(classConfig -> classConfig.tableName.equals(tableName) && classConfig.primarySharedId == primarySharedId)
                .findFirst();
        return optional.orElse(null);
    }
}
