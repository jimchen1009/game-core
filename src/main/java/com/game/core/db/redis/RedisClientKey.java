package com.game.core.db.redis;

import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.util.Objects;

/**
 * 
 */
public class RedisClientKey implements IRedisClientKey {

    private final String host;
    private final int port;

    public RedisClientKey(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisClientKey redisClientKey = (RedisClientKey) o;
        return port == redisClientKey.port && Objects.equals(host, redisClientKey.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    public static RedisClientKey create(Jedis jedis){
        Client client = jedis.getClient();
        return new RedisClientKey(client.getHost(), client.getPort());
    }

    public static RedisClientKey logCreate(Jedis jedis){
        if (jedis == null){
            return new RedisClientKey("", 0);
        }
        Client client = jedis.getClient();
        return new RedisClientKey(client.getHost(), client.getPort());
    }
}
