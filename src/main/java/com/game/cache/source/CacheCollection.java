package com.game.cache.source;

import com.game.cache.CollectionInfo;

import java.util.Collection;
import java.util.Map;

public class CacheCollection {

    private final Collection<Map<String, Object>> cacheValueList;
    private final CollectionInfo collectionInfo;

    public CacheCollection(Collection<Map<String, Object>> cacheValueList, CollectionInfo collectionInfo) {
        this.cacheValueList = cacheValueList;
        this.collectionInfo = collectionInfo;
    }

    public Collection<Map<String, Object>> getCacheValueList() {
        return cacheValueList;
    }

    public CollectionInfo getCollectionInfo() {
        return collectionInfo;
    }
}
