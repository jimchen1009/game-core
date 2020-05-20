package com.game.cache.source;

import com.game.cache.data.Data;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.IndexField;
import com.game.cache.mapper.annotation.IndexOptions;
import com.game.cache.mapper.annotation.IndexType;

@CacheIndex(fields = {@IndexField(name = "userId", type = IndexType.ASC, isPrimary = true)}, options = @IndexOptions(unique = true))
public class UserPlayer extends Data<Long> {

    @CacheFiled(index = 0)
    private long userId;

    @CacheFiled(index = 1)
    private String nickName;


    public UserPlayer(long userId, String nickName) {
        this.userId = userId;
        this.nickName = nickName;
    }

    public UserPlayer() {
        this(0, "");
    }

    @Override
    public Long secondaryKey() {
        return userId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}
