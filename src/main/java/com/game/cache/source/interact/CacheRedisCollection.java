package com.game.cache.source.interact;

import java.util.List;
import java.util.Map;

public class CacheRedisCollection {

    private final int primarySharedId;
    private List<Map.Entry<String, Object>> redisKeyValueList;


    public CacheRedisCollection(int primarySharedId, List<Map.Entry<String, Object>> redisKeyValueList) {
        this.primarySharedId = primarySharedId;
        this.redisKeyValueList = redisKeyValueList;
    }

    public int getPrimarySharedId() {
        return primarySharedId;
    }

    public List<Map.Entry<String, Object>> getRedisKeyValueList() {
        return redisKeyValueList;
    }
}
