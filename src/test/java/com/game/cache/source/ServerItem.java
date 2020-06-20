package com.game.cache.source;

import com.game.cache.data.Data;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.CacheIndexes;
import com.game.cache.mapper.annotation.IndexOptions;
import com.game.cache.mapper.annotation.PrimaryIndex;
import com.game.cache.mapper.annotation.SecondaryIndex;

@CacheIndexes(
        primaryIndex = @PrimaryIndex(primaryKey = "id", indexes = {@CacheIndex(name = "id")}),
        secondaryIndex = @SecondaryIndex(indexes = {@CacheIndex(name = "itemUniqueId")}),
        options = @IndexOptions(unique = true)
)
public class ServerItem extends Data<Long> {

    @CacheFiled(index = 0)
    private long id;

    @CacheFiled(index = 1)
    private long itemUniqueId;

    @CacheFiled(index = 2)
    private long count;

    public ServerItem(long id, long itemUniqueId, int count) {
        this.id = id;
        this.itemUniqueId = itemUniqueId;
        this.count = count;
    }

    public ServerItem() {
        this(0, 0, 0);
    }


    public void incCount(long count) {
        this.count += count;
        onIndexValueChanged(2);
    }

    public void decCount(long count) {
        this.count -= count;
        onIndexValueChanged(2);
    }

    @Override
    public Long secondaryKey() {
        return itemUniqueId;
    }

    public long getId() {
        return id;
    }

    public long getItemUniqueId() {
        return itemUniqueId;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", itemUniqueId=" + itemUniqueId +
                ", count=" + count +
                '}';
    }
}
