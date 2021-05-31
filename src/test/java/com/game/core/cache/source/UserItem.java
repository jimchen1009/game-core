package com.game.core.cache.source;

import com.game.core.cache.data.Data;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndexes;

@CacheIndexes(primaryKey = "userId", secondaryKeys = {"itemUniqueId"})
public class UserItem extends Data<Long> {

    @CacheFiled(index = 0)
    private long userId;

    @CacheFiled(index = 1)
    private long itemUniqueId;

    @CacheFiled(index = 2)
    private long count;

    @CacheFiled(index = 3)
    private long gainedCount;


    public UserItem(long userId, long itemUniqueId, int count) {
        this.userId = userId;
        this.itemUniqueId = itemUniqueId;
        this.count = count;
        this.gainedCount = count;
    }

    public UserItem() {
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
    public Long secondaryKey() {
        return itemUniqueId;
    }

    public long getUserId() {
        return userId;
    }

    public long getItemUniqueId() {
        return itemUniqueId;
    }

    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", itemUniqueId=" + itemUniqueId +
                '}';
    }
}
