package com.game.cache.source.redis;

import com.game.common.config.Configs;
import com.game.common.config.IConfig;
import com.game.db.redis.IRedisClient;
import com.game.db.redis.RedisClientManager;

public class RedisClientUtil {

    public static IRedisClient getRedisClient(){
        IConfig config = Configs.getInstance().getConfig("cache.redis");
        String name = config.getString("name");
        RedisClientManager clientManager = RedisClientManager.get(name);
        return clientManager.getClient();
    }
}
