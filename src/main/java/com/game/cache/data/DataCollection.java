package com.game.cache.data;

import com.game.cache.CacheInformation;

import java.util.List;

public class DataCollection<K, V extends IData<K>> {

    private final List<V> dataList;
    private final CacheInformation information;

    public DataCollection(List<V> dataList, CacheInformation information) {
        this.dataList = dataList;
        this.information = information;
    }

    public List<V> getDataList() {
        return dataList;
    }

    public CacheInformation getInformation() {
        return information;
    }

    public boolean isEmpty(){
        return information.isEmpty() && dataList.isEmpty();
    }

    public boolean isExpired(long currentTime){
        return information.isExpired(currentTime);
    }
}
