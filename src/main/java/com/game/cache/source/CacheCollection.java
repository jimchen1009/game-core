package com.game.cache.source;

import com.game.cache.CacheInformation;

import java.util.Collection;
import java.util.Map;

public class CacheCollection {

    private Collection<Map<String, Object>> cacheValueList;
    private final CacheInformation cacheInformation;

    public CacheCollection(Collection<Map<String, Object>> cacheValueList, CacheInformation cacheInformation) {
        this.cacheValueList = cacheValueList;
        this.cacheInformation = cacheInformation;
    }

    public Collection<Map<String, Object>> getCacheValuesList() {
        return cacheValueList;
    }

    public void setCacheValueList(Collection<Map<String, Object>> cacheValueList) {
        this.cacheValueList = cacheValueList;
    }

    public CacheInformation getCacheInformation() {
        return cacheInformation;
    }
}
