package com.game.db.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class MyRedisClient implements IRedisClient {

    interface IRedisCommand<T> {
        T execute(Jedis jedis) throws Exception;
    }


    interface IRedisMultiCommand<T> {
        void execute(T cache, Jedis jedis, Collection<String> keys) throws Exception;
    }


    protected abstract <T> T runCommand(String key, IRedisCommand command);

    protected abstract <T> T runMultiCommand(T cache, Collection<String> keys, IRedisMultiCommand<T> command);


    @Override
    public String set(String key, String value) {
        return runCommand(key, jedis -> jedis.set(key, value));
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return runCommand(key, jedis -> jedis.set(key, value, params));
    }

    @Override
    public String get(String key) {
        return runCommand(key, jedis -> jedis.get(key));
    }

    @Override
    public Boolean exists(String key) {
        return runCommand(key, jedis -> jedis.exists(key));
    }

    @Override
    public Long persist(String key) {
        return runCommand(key, jedis -> jedis.persist(key));
    }

    @Override
    public String type(String key) {
        return runCommand(key, jedis -> jedis.type(key));
    }

    @Override
    public Long expire(String key, int seconds) {
        return runCommand(key, jedis -> jedis.expire(key, seconds));
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return runCommand(key, jedis -> jedis.pexpire(key, milliseconds));
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return runCommand(key, jedis -> jedis.expireAt(key, unixTime));
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return runCommand(key, jedis -> jedis.pexpireAt(key, millisecondsTimestamp));
    }

    @Override
    public Long ttl(String key) {
        return runCommand(key, jedis -> jedis.ttl(key));
    }

    @Override
    public Long pttl(String key) {
        return runCommand(key, jedis -> jedis.pttl(key));
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        return runCommand(key, jedis -> jedis.setbit(key, offset, value));
    }

    @Override
    public Boolean setbit(String key, long offset, String value) {
        return runCommand(key, jedis -> jedis.setbit(key, offset, value));
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return runCommand(key, jedis -> jedis.getbit(key, offset));
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return runCommand(key, jedis -> jedis.setrange(key, offset, value));
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return runCommand(key, jedis -> jedis.getrange(key, startOffset, endOffset));
    }

    @Override
    public String getSet(String key, String value) {
        return runCommand(key, jedis -> jedis.getSet(key, value));
    }

    @Override
    public Long setnx(String key, String value) {
        return runCommand(key, jedis -> jedis.setnx(key, value));
    }

    @Override
    public String setex(String key, int seconds, String value) {
        return runCommand(key, jedis -> jedis.setex(key, seconds, value));
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return runCommand(key, jedis -> jedis.psetex(key, milliseconds, value));
    }

    @Override
    public Long decrBy(String key, long integer) {
        return runCommand(key, jedis -> jedis.decrBy(key, integer));
    }

    @Override
    public Long decr(String key) {
        return runCommand(key, jedis -> jedis.decr(key));
    }

    @Override
    public Long incrBy(String key, long integer) {
        return runCommand(key, jedis -> jedis.incrBy(key, integer));
    }

    @Override
    public Double incrByFloat(String key, double value) {
        return runCommand(key, jedis -> jedis.incrByFloat(key, value));
    }

    @Override
    public Long incr(String key) {
        return runCommand(key, jedis -> jedis.incr(key));
    }

    @Override
    public Long append(String key, String value) {
        return runCommand(key, jedis -> jedis.append(key, value));
    }

    @Override
    public String substr(String key, int start, int end) {
        return runCommand(key, jedis -> jedis.substr(key, start, end));
    }

    @Override
    public Long hset(String key, String field, String value) {
        return runCommand(key, jedis -> jedis.hset(key, field, value));
    }

    @Override
    public Long hset(String key, Map<String, String> hash) {
        return runCommand(key, jedis -> jedis.hset(key, hash));
    }

    @Override
    public String hget(String key, String field) {
        return runCommand(key, jedis -> jedis.hget(key, field));
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return runCommand(key, jedis -> jedis.hsetnx(key, field, value));
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return runCommand(key, jedis -> jedis.hmset(key, hash));
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return runCommand(key, jedis -> jedis.hmget(key, fields));
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return runCommand(key, jedis -> jedis.hincrBy(key, field, value));
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return runCommand(key, jedis -> jedis.hincrByFloat(key, field, value));
    }

    @Override
    public Boolean hexists(String key, String field) {
        return runCommand(key, jedis -> jedis.hexists(key, field));
    }

    @Override
    public Long hdel(String key, String... field) {
        return runCommand(key, jedis -> jedis.hdel(key, field));
    }

    @Override
    public Long hlen(String key) {
        return runCommand(key, jedis -> jedis.hlen(key));
    }

    @Override
    public Set<String> hkeys(String key) {
        return runCommand(key, jedis -> jedis.hkeys(key));
    }

    @Override
    public List<String> hvals(String key) {
        return runCommand(key, jedis -> jedis.hvals(key));
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return runCommand(key, jedis -> jedis.hgetAll(key));
    }

    @Override
    public List<Map.Entry<String, Object>> executeBatch(Consumer<IRedisPipeline> consumer) {
        return null;
    }
}
