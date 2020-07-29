package com.game.core.cache.source;

import com.game.core.cache.data.Data;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndex;
import com.game.core.cache.mapper.annotation.CacheIndexes;
import com.game.core.cache.mapper.annotation.IndexOptions;
import com.game.core.cache.mapper.annotation.PrimaryIndex;
import com.game.core.cache.mapper.annotation.SecondaryIndex;

import java.util.Arrays;

@CacheIndexes(
        primaryIndex = @PrimaryIndex(primaryKey = "id", indexes = {@CacheIndex(name = "id")}),
        secondaryIndex = @SecondaryIndex(indexes = {@CacheIndex(name = "currencyId")}),
        options = @IndexOptions(unique = true)
)
public class ServerCurrency extends Data<Integer> {

    @CacheFiled(index = 0)
    private long id;

    @CacheFiled(index = 1)
    private int currencyId;

    @CacheFiled(index = 2)
    private long count;

    @CacheFiled(index = 3)
    private long gainedCount;

    @CacheFiled(index = 4)
    private byte[] bytes;


    public ServerCurrency(long id, int currencyId, int count) {
        this.id = id;
        this.currencyId = currencyId;
        this.count = count;
        this.gainedCount = count;
        this.bytes = String.valueOf(currencyId).getBytes();
    }

    public ServerCurrency() {
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

    public long getId() {
        return id;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", currencyId=" + currencyId +
                ", count=" + count +
                ", gainedCount=" + gainedCount +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }
}
