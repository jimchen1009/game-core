package com.game.core.cache.source;

import com.game.core.cache.data.Data;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndexes;

@CacheIndexes(primaryKey = "id", secondaryKeys = {"itemUniqueId"})
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
    }

    public void decCount(long count) {
        this.count -= count;
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
