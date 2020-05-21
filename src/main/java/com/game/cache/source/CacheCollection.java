package com.game.cache.source;

import com.game.cache.CacheInformation;
import com.game.cache.InformationName;
import com.game.common.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheCollection {

    private Collection<Map<String, Object>> cacheValueList;
    private final CacheInformation information;

    public CacheCollection(Collection<Map<String, Object>> cacheValueList, CacheInformation information) {
        this.cacheValueList = cacheValueList;
        this.information = information;
    }

    public Collection<Map<String, Object>> getCacheValuesList() {
        return cacheValueList;
    }

    public void setCacheValueList(Collection<Map<String, Object>> cacheValueList) {
        this.cacheValueList = cacheValueList;
    }

    public CacheInformation getInformation() {
        return information;
    }


    public Map<Integer, CacheCollection> groupBySharedId(int primarySharedId){
        Map<Integer, List<Map<String, Object>>> key2CacheValues = CommonUtil.groupByKey(new HashMap<>(), cacheValueList,
                ArrayList::new, cacheValue -> (Integer) cacheValue.get(InformationName.CACHE_KEY.getKeyName()));
        Map<Integer, CacheCollection> primarySharedId2Collections = new HashMap<>();
        for (Map.Entry<Integer, List<Map<String, Object>>> entry : key2CacheValues.entrySet()) {
            CacheCollection cacheCollection = new CacheCollection(entry.getValue(), information.copy());
            primarySharedId2Collections.put(entry.getKey(), cacheCollection);
        }
        if (!primarySharedId2Collections.containsKey(primarySharedId)){
            primarySharedId2Collections.put(primarySharedId, new CacheCollection(new ArrayList<>(), information));
        }
        return primarySharedId2Collections;
    }
}
