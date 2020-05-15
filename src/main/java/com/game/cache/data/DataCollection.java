package com.game.cache.data;

import com.game.cache.CacheInformation;

import java.util.List;

public class DataCollection<K, V extends Data<K>> {

    private final List<V> valueList;
    private final CacheInformation information;

    public DataCollection(List<V> valueList, CacheInformation information) {
        this.valueList = valueList;
        this.information = information;
    }

    public List<V> getValueList() {
        return valueList;
    }

    public CacheInformation getInformation() {
        return information;
    }
}
