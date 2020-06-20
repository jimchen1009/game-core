package com.game.cache.source;

import com.game.cache.data.Data;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.CacheIndexes;
import com.game.cache.mapper.annotation.IndexOptions;
import com.game.cache.mapper.annotation.PrimaryIndex;
import com.game.cache.mapper.annotation.SecondaryIndex;

@CacheIndexes(
        primaryIndex = @PrimaryIndex(primaryKey = "userId", indexes = {@CacheIndex(name = "userId")}),
        secondaryIndex = @SecondaryIndex(indexes = {@CacheIndex(name = "id")}),
        options = @IndexOptions(unique = true)
)
public class UserActBoss extends Data<Long> {

    @CacheFiled(index = 0)
    private long userId;

    @CacheFiled(index = 1)
    private long id;

    @CacheFiled(index = 2)
    private long hp;

    @CacheFiled(index =3)
    private long thp;



    @Override
    public Long secondaryKey() {
        return id;
    }


    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", id=" + id +
                ", hp=" + hp +
                ", thp=" + thp +
                '}';
    }
}
