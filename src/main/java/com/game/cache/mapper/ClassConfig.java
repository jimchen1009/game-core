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

    public final boolean isUserClass;
    public final String className;
    public final String tableName;
    public final boolean enableRedis;
    public final int primarySharedId;
    public final boolean delayUpdate;
    public final boolean loadOnShared;
    public final int versionId;

    private ClassConfig(boolean isUserClass, String className, String tableName, boolean enableRedis, int primarySharedId, boolean delayUpdate,
                        boolean loadOnShared, int versionId) {
        this.isUserClass = isUserClass;
        this.className = className;
        this.tableName = tableName;
        this.enableRedis = enableRedis;
        this.primarySharedId = primarySharedId;
        this.delayUpdate = delayUpdate;
        this.loadOnShared = loadOnShared;
        this.versionId = versionId;
    }

    public int getPrimarySharedId() {
        return primarySharedId;
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
    private static final Map<String, List<Integer>> name2SharedIdList = new HashMap<>();
    static {
        List<IConfig> configList = Configs.getInstance().getConfigList("cache.classes");
        for (IConfig config : configList) {
            boolean isUserClass = config.getBoolean("userClass");
            String tableName = config.getString("tableName");
            boolean enableRedis = config.getBoolean("enableRedis");
            List<Integer> loadOnSharedId = config.getList("loadOnSharedId");
            List<IConfig> sharedClassesConfigList = config.getConfigList("sharedClasses");
            for (IConfig classesConfig : sharedClassesConfigList) {
                String className = classesConfig.getString("className");
                int primarySharedId = classesConfig.getInt("primarySharedId");
                boolean delayUpdate = classesConfig.getBoolean("delayUpdate");
                boolean loadOnShared = loadOnSharedId.contains(primarySharedId);
                int versionId = classesConfig.getInt("versionId");
                ClassConfig classConfig = new ClassConfig(isUserClass, className, tableName, enableRedis, primarySharedId, delayUpdate, loadOnShared, versionId);
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
            name2SharedIdList.put(entry.getKey(), sharedIdList);
        }
    }

    public static String getRedisPatternString(){
        return "";
    }

    public static ClassConfig getConfig(Class<?> aClass){
        return class2Configs.get(aClass.getName());
    }

    public static List<Integer> getPrimarySharedIdList(String tableName){
        List<Integer> sharedIdList = name2SharedIdList.get(tableName);
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
