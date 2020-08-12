package com.game.core.cache.source.redis;

import com.game.common.config.CoreConfigs;
import com.game.common.config.IConfig;
import com.game.core.db.redis.IRedisClient;
import com.game.core.db.redis.RedisClientManager;

public class RedisClientUtil {

    public static IRedisClient getRedisClient(){
        IConfig config = CoreConfigs.getConfig("cache.redis");
        String name = config.getString("name");
        RedisClientManager clientManager = RedisClientManager.get(name);
        return clientManager.getClient();
    }
}
