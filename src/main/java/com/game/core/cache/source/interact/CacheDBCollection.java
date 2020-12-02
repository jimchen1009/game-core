package com.game.core.cache.source.interact;

import com.game.core.cache.CacheName;
import com.game.common.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheDBCollection {

    private final int primarySharedId;
    private Collection<Map<String, Object>> cacheValueList;

    public CacheDBCollection(int primarySharedId, Collection<Map<String, Object>> cacheValueList) {
        this.primarySharedId = primarySharedId;
        this.cacheValueList = cacheValueList;
    }

    public int getPrimarySharedId() {
        return primarySharedId;
    }

    public Collection<Map<String, Object>> getCacheValuesList() {
        return cacheValueList;
    }

    public static Map<Integer, List<Map<String, Object>>> groupPrimarySharedId(Collection<Map<String, Object>> cacheValueList){
        return CommonUtil.splitUp1Into1Group(new HashMap<>(), cacheValueList,
                ArrayList::new, cacheValue -> (Integer) cacheValue.get(CacheName.PrimaryId.getKeyName()));
    }
}
