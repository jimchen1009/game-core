package com.game.core.cache.source;

import com.game.core.cache.data.Data;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndexes;

@CacheIndexes(primaryKey = "userId", secondaryKeys = {"id"})
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
