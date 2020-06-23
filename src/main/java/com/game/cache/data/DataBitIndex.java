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
        uniqueIdBitIndex.put(RedisChanged.getId(), RedisChanged);
    }

    private final int id;

    private DataBitIndex(int id) {
        if (id > 63){
            throw new IllegalArgumentException(String.valueOf(id));
        }
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static DataBitIndex getBitIndex(int uniqueId){
        return uniqueIdBitIndex.computeIfAbsent(uniqueId, DataBitIndex::new);
    }
}
