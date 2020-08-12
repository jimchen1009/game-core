package com.game.core.db.redis;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.IEvnConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisClientManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisClientManager.class);

    private static final Map<String, RedisClientManager> managers = new HashMap<>();

    public static void init(){
        List<IEvnConfig> configList = EvnCoreConfigs.getConfigList("db.redis");
        for (IEvnConfig iEvnConfig : configList) {
            RedisClientManager manager = new RedisClientManager(iEvnConfig);
            for (String s : manager.names) {
                managers.put(s, manager);
            }
        }
    }

    public static void destroy(){
        for (RedisClientManager manager : managers.values()) {
            manager.getClient().destroy();
        }
    }

    public static RedisClientManager get(String name){
        return managers.get(name);
    }

    private final IRedisClient client;
    private final List<String> names;

    private RedisClientManager(IEvnConfig redisConfig) {
        this.names = Collections.unmodifiableList(redisConfig.getList("names"));
        this.client = new MyShardedJedisPool(redisConfig);
    }

    public IRedisClient getClient() {
        return client;
    }
}
