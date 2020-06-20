package com.game.cache.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBitIndex {

    /**
     * 预留位置的最大值Index
     */
    public static final int MaximumIndex = 60;
    /**
     * Redis的數據是否變化了
     */
    public static DataBitIndex RedisChanged = new DataBitIndex(63);


    private static final Map<Integer, DataBitIndex> uniqueIdBitIndex = new ConcurrentHashMap<>();
    static {
        uniqueIdBitIndex.put(RedisChanged.getUniqueId(), RedisChanged);
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
