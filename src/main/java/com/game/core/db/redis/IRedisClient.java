package com.game.core.db.redis;

import com.game.common.config.IEvnConfig;
import redis.clients.jedis.params.SetParams;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface IRedisClient {

    void reload(IEvnConfig config);

    void destroy();

    String set(String key, String value);

    String set(String key, String value, SetParams params);

    String get(String key);

    Boolean exists(String key);

    Long persist(String key);

    String type(String key);

    Long expire(String key, int seconds);

    Long pexpire(String key, long milliseconds);

    Long expireAt(String key, long unixTime);

    Long pexpireAt(String key, long millisecondsTimestamp);

    Long ttl(String key);

    Long pttl(final String key);

    Boolean setbit(String key, long offset, boolean value);

    Boolean setbit(String key, long offset, String value);

    Boolean getbit(String key, long offset);

    Long setrange(String key, long offset, String value);

    String getrange(String key, long startOffset, long endOffset);

    String getSet(String key, String value);

    Long setnx(String key, String value);

    String setex(String key, int seconds, String value);

    String psetex(final String key, final long milliseconds, final String value);

    Long decrBy(String key, long integer);

    Long decr(String key);

    Long incrBy(String key, long integer);

    Double incrByFloat(String key, double value);

    Long incr(String key);

    Long append(String key, String value);

    String substr(String key, int start, int end);

    Long hset(String key, String field, String value);

    Long hset(final String key, final Map<String, String> hash);

    String hget(String key, String field);

    Long hsetnx(String key, String field, String value);

    String hmset(String key, Map<String, String> hash);

    List<String> hmget(String key, String... fields);

    Long hincrBy(String key, String field, long value);

    Double hincrByFloat(final String key, final String field, final double value);

    Boolean hexists(String key, String field);

    Long hdel(String key, String... field);

    Long hlen(String key);

    Set<String> hkeys(String key);

    List<String> hvals(String key);

    Map<String, String> hgetAll(String key);

    List<Map.Entry<String, Object>> executeBatch(Consumer<IRedisPipeline> consumer);
}
