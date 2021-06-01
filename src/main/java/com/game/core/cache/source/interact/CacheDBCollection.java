package com.game.core.cache.source.interact;

import java.util.Collection;
import java.util.Map;

public class CacheDBCollection {

    private Collection<Map<String, Object>> cacheValueList;

    public CacheDBCollection(Collection<Map<String, Object>> cacheValueList) {
        this.cacheValueList = cacheValueList;
    }

    public Collection<Map<String, Object>> getCacheValuesList() {
        return cacheValueList;
    }
}
