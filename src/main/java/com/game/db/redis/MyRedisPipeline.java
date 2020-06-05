package com.game.db.redis;

import redis.clients.jedis.commands.RedisPipeline;

public class MyRedisPipeline implements IRedisPipeline {

    private final RedisPipeline pipeline;

    public MyRedisPipeline(RedisPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void hgetAll(String key) {
        pipeline.hgetAll(key);
    }

    @Override
    public void ttl(String key) {
        pipeline.ttl(key);
    }

    @Override
    public void pttl(String key) {
        pipeline.pttl(key);
    }
}
