package com.game.core.cache.data;

import java.util.List;

public class DataCollection<K, V extends IData<K>> {

    private final List<V> dataList;

    public DataCollection(List<V> dataList) {
        this.dataList = dataList;
    }

    public List<V> getDataList() {
        return dataList;
    }

    public DataCollection<K, V> updateDataList(List<V> dataList){
        return new DataCollection<>(dataList);
    }
}
