package com.game.cache.data;

import com.game.cache.CacheUniqueId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBitIndex {


    private static final Map<Integer, DataBitIndex> uniqueIdBitIndex = new ConcurrentHashMap<>();

    public static DataBitIndex CacheCreated = new DataBitIndex(CacheUniqueId.MAX_ID + 1);
    public static DataBitIndex RedisChanged = new DataBitIndex(CacheUniqueId.MAX_ID + 2);
    public static DataBitIndex RedisDeleted = new DataBitIndex(CacheUniqueId.MAX_ID + 3);

    static {
        uniqueIdBitIndex.put(CacheCreated.getUniqueId(), CacheCreated);
        uniqueIdBitIndex.put(RedisChanged.getUniqueId(), RedisChanged);
        uniqueIdBitIndex.put(RedisDeleted.getUniqueId(), RedisDeleted);
    }

    private final int uniqueId;

    private DataBitIndex(int uniqueId) {
        if (uniqueId > 63){
            throw new IllegalArgumentException(String.valueOf(uniqueId));
        }
        this.uniqueId = uniqueId;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public static DataBitIndex getBitIndex(int uniqueId){
        return uniqueIdBitIndex.computeIfAbsent(uniqueId, DataBitIndex::new);
    }
}
