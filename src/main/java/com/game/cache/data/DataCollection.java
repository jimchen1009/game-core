package com.game.cache.data;

import com.game.cache.CollectionInfo;

import java.util.List;

public class DataCollection<K, V extends Data<K>> {

    private final List<V> valueList;
    private final CollectionInfo information;

    public DataCollection(List<V> valueList, CollectionInfo information) {
        this.valueList = valueList;
        this.information = information;
    }

    public List<V> getValueList() {
        return valueList;
    }

    public CollectionInfo getInformation() {
        return information;
    }
}
