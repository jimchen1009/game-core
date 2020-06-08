package com.game.cache.source;

import com.game.cache.data.Data;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.IndexField;
import com.game.cache.mapper.annotation.IndexOptions;
import com.game.cache.mapper.annotation.IndexType;

@CacheIndex(fields = {@IndexField(name = "userId", type = IndexType.ASC, isPrimary = true), @IndexField(name = "currencyId", type = IndexType.ASC, isPrimary = false)}, options = @IndexOptions(unique = true))
public class UserCurrency extends Data<Integer> {

    @CacheFiled(index = 0)
    private long userId;

    @CacheFiled(index = 1)
    private int currencyId;

    @CacheFiled(index = 2)
    private long count;

    @CacheFiled(index = 3)
    private long gainedCount;


    public UserCurrency(long userId, int currencyId, int count) {
        this.userId = userId;
        this.currencyId = currencyId;
        this.count = count;
        this.gainedCount = count;
    }

    public UserCurrency() {
        this(0, 0, 0);
    }


    public void incCount(long count) {
        this.count += count;
        this.gainedCount += count;
        onIndexValueChanged(2);
        onIndexValueChanged(3);
    }

    public void decCount(long count) {
        this.count -= count;
        onIndexValueChanged(2);
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
