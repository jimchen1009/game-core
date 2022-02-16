package com.game.core.db.redis;

import com.game.common.config.IEvnConfig;
import com.game.core.cache.exception.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MyShardedJedisPool extends MyJedisClientPool<ShardedJedis> {

    private ShardedJedisPool pool;

    public MyShardedJedisPool(IEvnConfig redisConfig) {
        reload(redisConfig);
    }

    @Override
    public synchronized void reload(IEvnConfig redisConfig) {
        List<IEvnConfig> addressConfigList = redisConfig.getConfigList("sharding");
        List<JedisShardInfo> shardInfoList = addressConfigList.stream().map( config -> {
            JedisShardInfo jedisShardInfo = new JedisShardInfo(config.getString("host"), config.getInt("port"));
            if (config.hasPath("password")) {
                String password = config.getString("password");
                if (!password.isEmpty()){
                    jedisShardInfo.setPassword(password);
                }
            }
            return jedisShardInfo;
        }).collect(Collectors.toList());
        JedisPoolConfig poolConfig = getPoolConfig(redisConfig);
        this.destroy();
//        this.pool = new ShardedJedisPool(poolConfig, shardInfoList, Pattern.compile(ClassConfig.getRedisPatternString()));
        ShardedJedisPool oldPool = this.pool;
        this.pool = new ShardedJedisPool(poolConfig, shardInfoList);
        if (oldPool != null){
            oldPool.destroy();
        }
    }

    @Override
    public synchronized void destroy() {
        if (pool == null) {
            return;
        }
        pool.destroy();
        pool = null;
    }

    @Override
    public List<Map.Entry<String, Object>> executeBatch(Consumer<IRedisPipeline> consumer) {
        return execute( jedis -> {
            MyRedisPipeline redisPipeline = new MyRedisPipeline(jedis.pipelined());
            consumer.accept(redisPipeline);
            return redisPipeline.syncResponse();
        }, "executeBatch");
    }

    @Override
    public ShardedJedis getResource() {
        return pool.getResource();
    }

    @Override
    protected void returnResource(ShardedJedis resource) {
        resource.close();
    }


    @SuppressWarnings("unchecked")
    @Override
    protected <T> T runCommand(String key, IRedisCommand command) {
        return execute( shardedJedis -> {
            Jedis jedis = shardedJedis.getShard(key);
            try {
                return (T)command.execute(jedis);
            }
            catch (Exception e) {
                throw new CacheException("redis command error. key:%s, node:%s", e, key, RedisClientKey.logCreate(jedis));
            }
        }, "runCommand");
    }

    @Override
    protected <T> T runMultiCommand(T cache, Collection<String> keys, IRedisMultiCommand<T> command) {
        return execute( shardedJedis -> {
            Jedis jedis = null;
            try {
                Map<Jedis, List<String>> jedis2Keys = new HashMap<>();
                for (String key : keys) {
                    jedis = shardedJedis.getShard(key);
                    List<String> keyList = jedis2Keys.computeIfAbsent(jedis, k -> new ArrayList<>());
                    keyList.add(key);
                }
                for (Map.Entry<Jedis, List<String>> entry : jedis2Keys.entrySet()) {
                    jedis = entry.getKey();
                    command.execute(cache, jedis, entry.getValue());
                }
                return cache;
            }
            catch (Exception e) {
                throw new RedisException("multi command error. key:%s, node:%s" , e, RedisClientKey.logCreate(jedis));
            }
        }, "runMultiCommand");
    }



    public class MyRedisPipeline implements IRedisPipeline {

        private final ShardedJedisPipeline pipeline;
        private final List<Map.Entry<String, Response<?>>> responseList;

        public MyRedisPipeline(ShardedJedisPipeline pipeline) {
            this.pipeline = pipeline;
            this.responseList = new ArrayList<>();
        }

        @Override
        public void hgetAll(String key) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.hgetAll(key)));
        }

        @Override
        public void hset(String key, String field, String value) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.hset(key, field, value)));
        }

        @Override
        public void hset(String key, Map<String, String> hash) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.hset(key, hash)));
        }

        @Override
        public void ttl(String key) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.ttl(key)));
        }

        @Override
        public void pttl(String key) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.pttl(key)));
        }

        @Override
        public void pexpireAt(String key, long millisecondsTimestamp) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.pexpireAt(key, millisecondsTimestamp)));
        }

        @Override
        public void del(String key) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.del(key)));
        }

        @Override
        public void hdel(String key, String... field) {
            responseList.add(new AbstractMap.SimpleEntry<>(key, pipeline.hdel(key, field)));
        }

        @Override
        public int commandCount() {
            return responseList.size();
        }

        public List<Map.Entry<String, Object>> syncResponse() {
            if (responseList.isEmpty()) {
                return new ArrayList<>();
            }
            pipeline.sync();
            List<Map.Entry<String, Object>> resultList = new ArrayList<>(responseList.size());
            for (Map.Entry<String, Response<?>> entry : responseList) {
                Object object = entry.getValue().get();
                resultList.add(new AbstractMap.SimpleEntry<>(entry.getKey(), object));
            }
            return resultList;
        }
    }
}
