package com.game.core.cache.source;

import com.game.core.cache.data.Data;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndexes;

@CacheIndexes(primaryKey = "userId", secondaryKeys = {"currencyId"})
public class UserCurrency extends Data<Integer> {

    @CacheFiled(index = 0)
    private long userId;

    @CacheFiled(index = 1)
    private int currencyId;

    @CacheFiled(index = 2)
    private long count;

    @CacheFiled(index = 3)
    private long gainedCount;

    @CacheFiled(index = 4)
    private byte[] bytes;


    public UserCurrency(long userId, int currencyId, int count) {
        this.userId = userId;
        this.currencyId = currencyId;
        this.count = count;
        this.gainedCount = count;
        this.bytes = String.valueOf(currencyId).getBytes();
    }

    public UserCurrency() {
        this(0, 0, 0);
    }


    public void incCount(long count) {
        this.count += count;
        this.gainedCount += count;
    }

    public void decCount(long count) {
        this.count -= count;
    }

    @Override
    public Integer secondaryKey() {
        return currencyId;
    }

    public long getUserId() {
        return userId;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", currencyId=" + currencyId +
                ", count=" + count +
                ", gainedCount=" + gainedCount +
                '}';
    }
}
