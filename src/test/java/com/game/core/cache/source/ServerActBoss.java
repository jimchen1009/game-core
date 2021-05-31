package com.game.core.cache.source;

import com.game.core.cache.data.Data;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndexes;

@CacheIndexes(primaryKey = "zoneId", secondaryKeys = {"id"})
public class ServerActBoss extends Data<Long> {

    @CacheFiled(index = 0)
    private long zoneId;

    @CacheFiled(index = 1)
    private long id;

    @CacheFiled(index = 2)
    private long hp;

    @CacheFiled(index =32)
    private long thp;



    @Override
    public Long secondaryKey() {
        return id;
    }

    public long getId() {
        return id;
    }


    @Override
    public String toString() {
        return "{" +
                "zoneId=" + zoneId +
                ", id=" + id +
                ", hp=" + hp +
                ", thp=" + thp +
                '}';
    }
}
