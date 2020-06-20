package com.game.cache.config;

import com.game.cache.CacheType;
import com.game.common.config.ConfigUtil;
import com.game.common.config.Configs;
import com.game.common.config.IConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClassConfig {


    private static Map<String, List<ClassConfig>> name2ConfigList = new HashMap<>();
    private static Map<String, ClassConfig> class2Configs = new HashMap<>();
    private static Map<String, List<Integer>> name2SharedIdList = new HashMap<>();

    static{
        CacheType defaultCacheType = CacheType.valueOf(Configs.getInstance().getString("cache.type"));
        String path = Configs.getInstance().getString("cache.class_config");
        Map<String, IConfig> loadConfigs = ConfigUtil.loadConfigs(path);
        Pattern pattern = Pattern.compile("nodb[0-9]+");
        for (Map.Entry<String, IConfig> entry : loadConfigs.entrySet()) {
            String filename = entry.getKey();
            int lastIndexOf = filename.lastIndexOf(".");
            String[] strings = filename.substring(0, lastIndexOf).split("_");
            String accountString = strings[0].trim();
            boolean accountCache;
            if (accountString.equals("account")){
                accountCache = true;
            }
            else if (accountString.equals("common")){
                accountCache = false;
            }
            else {
                throw new RuntimeException(filename);
            }
            String name = strings[1].trim();
            CacheType cacheType = pattern.matcher(name).matches() ? CacheType.Redis : defaultCacheType;

            IConfig entryValue = entry.getValue();
            boolean redisSupport = entryValue.hasPath("redisSupport") && entryValue.getBoolean("redisSupport");
            List<IConfig> configList = entryValue.getConfigList("sharedClasses");
            List<ClassConfig> classConfigList = new ArrayList<>();
            for (IConfig config : configList) {
                ClassConfig classConfig = new ClassConfig(cacheType, name, accountCache, redisSupport, config);
                class2Configs.put(classConfig.getClassName(), classConfig);
                classConfigList.add(classConfig);
            }
            classConfigList.sort(Comparator.comparingInt(ClassConfig::getPrimarySharedId));
            name2ConfigList.put(name, classConfigList);
        }

        for (Map.Entry<String, List<ClassConfig>> entry : name2ConfigList.entrySet()) {
            List<Integer> primarySharedIds = entry.getValue().stream().map(ClassConfig::getPrimarySharedId).collect(Collectors.toList());
            if (primarySharedIds.size() < 2){
                continue;
            }
            if (new HashSet<>(primarySharedIds).size() < primarySharedIds.size()) {
                throw new IllegalArgumentException(entry.getValue() + "");
            }
            if (primarySharedIds.contains(0)){
                throw new IllegalArgumentException(entry.getValue() + "");
            }
            name2SharedIdList.put(entry.getKey(), primarySharedIds);
        }
    }

    public static String getRedisPatternString(){
        return "";
    }

    public static ClassConfig getConfig(Class<?> aClass){
        return class2Configs.get(aClass.getName());
    }

    public static List<Integer> getPrimarySharedIdList(String name){
        List<Integer> sharedIdList = name2SharedIdList.get(name);
        return sharedIdList == null ? new ArrayList<>(0) : new ArrayList<>(sharedIdList);
    }

    public static List<ClassConfig> getPrimarySharedConfigList(String name){
        List<ClassConfig> classConfigs = name2ConfigList.get(name);
        return classConfigs == null ? new ArrayList<>(0) : new ArrayList<>(classConfigs);
    }

    public final CacheType cacheType;
    public final String name;
    public final boolean accountCache;
    public final boolean redisSupport;

    public final String className;
    public final int primarySharedId;
    public final boolean delayUpdate;
    public final int versionId;


    public ClassConfig(CacheType cacheType, String name, boolean accountCache, boolean redisSupport, IConfig config) {
        this.cacheType = cacheType;
        this.name = name;
        this.accountCache = accountCache;
        this.redisSupport = redisSupport;
        this.className = config.getString("className");
        this.primarySharedId = config.getInt("primarySharedId");
        this.versionId = config.getInt("versionId");
        this.delayUpdate = config.getBoolean("delayUpdate");
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public String getName() {
        return name;
    }

    public boolean isAccountCache() {
        return accountCache;
    }

    public boolean isRedisSupport() {
        return redisSupport;
    }

    public String getClassName() {
        return className;
    }

    public int getPrimarySharedId() {
        return primarySharedId;
    }

    public int getVersionId() {
        return versionId;
    }

    public boolean isDelayUpdate() {
        return delayUpdate;
    }


    @Override
    public String toString() {
        return "{" +
                "cacheType=" + cacheType.name() +
                ", name='" + name + '\'' +
                ", accountCache=" + accountCache +
                ", redisSupport=" + redisSupport +
                ", className='" + className + '\'' +
                ", primarySharedId=" + primarySharedId +
                ", delayUpdate=" + delayUpdate +
                ", versionId=" + versionId +
                '}';
    }
}
