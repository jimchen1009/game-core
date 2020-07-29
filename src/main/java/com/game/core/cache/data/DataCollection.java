package com.game.core.cache.data;

import com.game.core.cache.CacheInformation;

import java.util.List;

public class DataCollection<K, V extends IData<K>> {

    private final List<V> dataList;
    private final CacheInformation cacheInformation;

    public DataCollection(List<V> dataList, CacheInformation cacheInformation) {
        this.dataList = dataList;
        this.cacheInformation = cacheInformation;
    }

    public List<V> getDataList() {
        return dataList;
    }

    public CacheInformation getCacheInformation() {
        return cacheInformation;
    }

    public boolean isExpired(long currentTime){
        return cacheInformation.isExpired(currentTime);
    }
}
