package com.game.cache.source;

import com.game.cache.data.Data;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.CacheIndexes;
import com.game.cache.mapper.annotation.IndexOptions;
import com.game.cache.mapper.annotation.PrimaryIndex;
import com.game.cache.mapper.annotation.SecondaryIndex;

@CacheIndexes(
        primaryIndex = @PrimaryIndex(primaryKey = "zoneId", indexes = {@CacheIndex(name = "zoneId")}),
        secondaryIndex = @SecondaryIndex(indexes = {@CacheIndex(name = "id")}),
        options = @IndexOptions(unique = true)
)
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
