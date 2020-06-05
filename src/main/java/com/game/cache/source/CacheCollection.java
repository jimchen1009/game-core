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

    private final int primarySharedId;
    private Collection<Map<String, Object>> cacheValueList;
    private final CacheInformation information;

    public CacheCollection(int primarySharedId, Collection<Map<String, Object>> cacheValueList, CacheInformation information) {
        this.primarySharedId = primarySharedId;
        this.cacheValueList = cacheValueList;
        this.information = information;
    }

    public int getPrimarySharedId() {
        return primarySharedId;
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

    public boolean isEmpty(){
        return information.isEmpty() && cacheValueList.isEmpty();
    }

    public static Map<Integer, List<Map<String, Object>>> groupPrimarySharedId(Collection<Map<String, Object>> cacheValueList){
        return CommonUtil.groupByKey(new HashMap<>(), cacheValueList,
                ArrayList::new, cacheValue -> (Integer) cacheValue.get(InformationName.CACHE_KEY.getKeyName()));
    }
}
