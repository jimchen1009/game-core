package com.game.core.cache.source.redis;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;
import com.game.common.config.IEvnConfig;
import com.game.core.db.redis.IRedisClient;
import com.game.core.db.redis.RedisClientManager;

public class RedisClientUtil {

    public static IRedisClient getRedisClient(){
        IEvnConfig config = EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getConfig("redis");
        String name = config.getString("name");
        RedisClientManager clientManager = RedisClientManager.get(name);
        return clientManager.getClient();
    }
}
