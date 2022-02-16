package com.game.core.cache.source;

import com.game.core.cache.data.Data;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndexes;

@CacheIndexes(primaryKey = "userId")
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

    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}
