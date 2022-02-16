package com.game.core.cache.source.redis;

import com.game.core.db.redis.IRedisClient;
import com.game.core.db.redis.RedisClientManager;

public class RedisUtil {

    public static IRedisClient getClient(){
        RedisClientManager clientManager = RedisClientManager.get("cache");
        return clientManager.getClient();
    }
}
