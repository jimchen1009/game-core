package com.game.core.cache;

import java.util.Objects;

public class ClassConfig implements IClassConfig {


    private final Class<?> aClass;
    private CacheType cacheType;
    private String name;
    private boolean accountCache;
    private boolean cacheLoadAdvance;
    private boolean redisSupport;
    private long redisDuration;
    private boolean delayUpdate;
    private int versionId;

    public ClassConfig(Class<?> aClass) {
        this.aClass = aClass;
        this.cacheType = CacheType.Memory;
        this.accountCache = false;
        this.cacheLoadAdvance = false;
        this.redisSupport = false;
        this.redisDuration = 0;
        this.versionId = 1;
        this.delayUpdate = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Class<V> getAClass() {
        return (Class<V>) aClass;
    }

    @Override
    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    @Override
    public String getName() {
        return name;
    }

    public ClassConfig setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean isAccountCache() {
        return accountCache;
    }

    public ClassConfig setAccountCache(boolean accountCache) {
        this.accountCache = accountCache;
        return this;
    }

    @Override
    public boolean isCacheLoadAdvance() {
        return cacheLoadAdvance;
    }

    public ClassConfig setCacheLoadAdvance(boolean cacheLoadAdvance) {
        this.cacheLoadAdvance = cacheLoadAdvance;
        return this;
    }

    @Override
    public boolean isRedisSupport() {
        return redisSupport && cacheType.isDB();
    }

    public ClassConfig setRedisSupport(boolean redisSupport) {
        this.redisSupport = redisSupport;
        return this;
    }

    @Override
    public long getRedisDuration() {
        return redisDuration;
    }

    public ClassConfig setRedisDuration(long redisDuration) {
        this.redisDuration = redisDuration;
        return this;
    }

    @Override
    public boolean isDelayUpdate() {
        return delayUpdate;
    }

    public ClassConfig setDelayUpdate(boolean delayUpdate) {
        this.delayUpdate = delayUpdate;
        return this;
    }

    @Override
    public int getVersionId() {
        return versionId;
    }

    public ClassConfig setVersionId(int versionId) {
        this.versionId = versionId;
        return this;
    }

    @Override
    public ClassConfig cloneConfig() {
        ClassConfig classConfig = new ClassConfig(aClass);
        classConfig.cacheType = cacheType;
        classConfig.name = name;
        classConfig.accountCache = accountCache;
        classConfig.cacheLoadAdvance = cacheLoadAdvance;
        classConfig.redisSupport = redisSupport;
        classConfig.delayUpdate = delayUpdate;
        classConfig.versionId = versionId;
        return classConfig;
    }

    @Override
    public String toString() {
        return "{" +
                "aClass=" + aClass.getName() +
                ", cacheType=" + cacheType.name() +
                ", name='" + name + '\'' +
                ", accountCache=" + accountCache +
                ", redisSupport=" + redisSupport +
                ", delayUpdate=" + delayUpdate +
                ", versionId=" + versionId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConfig that = (ClassConfig) o;
        return accountCache == that.accountCache &&
                redisSupport == that.redisSupport &&
                delayUpdate == that.delayUpdate &&
                versionId == that.versionId &&
                Objects.equals(aClass, that.aClass) &&
                cacheType == that.cacheType &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, accountCache, redisSupport, delayUpdate, versionId);
    }
}
