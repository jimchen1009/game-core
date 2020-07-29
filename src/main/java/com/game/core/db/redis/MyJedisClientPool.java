package com.game.core.db.redis;

import com.game.common.config.IConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.commands.JedisCommands;

import java.util.function.Function;

public abstract class MyJedisClientPool<T extends JedisCommands> extends MyRedisClient {

    private static final Logger logger = LoggerFactory.getLogger(MyJedisClientPool.class);

    public abstract T getResource() ;

    protected abstract void returnResource(T resource);

    protected  <V> V execute(Function<T, V> function, String message){
        T resource = null;
        try {
            resource = getResource();
            return function.apply(resource);
        }
        finally {
            if (resource != null) {
                returnResource(resource);
            }
        }
    }

    protected JedisPoolConfig getPoolConfig(IConfig redisConfig){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        int maxConnection = redisConfig.getInt("maxConnection");
        jedisPoolConfig.setMaxIdle(maxConnection);
        jedisPoolConfig.setMinIdle(maxConnection);
        jedisPoolConfig.setMaxTotal(maxConnection);
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setTestOnCreate(true);
        return jedisPoolConfig;
    }
}
