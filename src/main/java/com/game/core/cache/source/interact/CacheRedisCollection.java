package com.game.core.cache.source.interact;

import com.game.core.cache.CacheInformation;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.db.redis.IRedisPipeline;

import java.util.List;
import java.util.Map;

public class CacheRedisCollection {

    public static final String ExpiredName = "ttl.expired";

    private final Map<String, String> redisKeyValueMap;
    private final CacheInformation cacheInformation;

    public CacheRedisCollection(Map<String, String> redisKeyValueMap, CacheInformation cacheInformation) {
        this.redisKeyValueMap = redisKeyValueMap;
        this.cacheInformation = cacheInformation;
    }

    public boolean isEmpty(){
        return redisKeyValueMap == null || redisKeyValueMap.isEmpty();
    }

    /**
     * 执行对应的命令获取数据
     * @param primaryKey
     * @param redisPipeline
     * @param cacheUniqueId
     */
    public static void executeCommand(long primaryKey, IRedisPipeline redisPipeline, ICacheUniqueId cacheUniqueId){
        String redisKeyString = cacheUniqueId.getRedisKeyString(primaryKey);
        redisPipeline.hgetAll(redisKeyString);
    }


    @SuppressWarnings("unchecked")
    public static CacheRedisCollection readCollection(List<Map.Entry<String, Object>> entryList){
        Map<String, String> redisKeyValueMap = (Map<String, String>)entryList.get(0).getValue();
        CacheInformation cacheInformation = new CacheInformation();
        String expiredTime = redisKeyValueMap.remove(ExpiredName);
        if (expiredTime != null){
            cacheInformation.updateCurrentTime(Long.parseLong(expiredTime));
        }
        return new CacheRedisCollection(redisKeyValueMap, cacheInformation);
    }
}
