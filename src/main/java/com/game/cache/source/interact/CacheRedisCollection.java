package com.game.cache.source.interact;

import java.util.List;
import java.util.Map;

public class CacheRedisCollection {

    private List<Map.Entry<String, Object>> redisKeyValueList;

    public CacheRedisCollection(List<Map.Entry<String, Object>> redisKeyValueList) {
        this.redisKeyValueList = redisKeyValueList;
    }

    public List<Map.Entry<String, Object>> getRedisKeyValueList() {
        return redisKeyValueList;
    }
}
