package com.game.db.redis;

public interface IRedisPipeline {

    void hgetAll(String key);

    void ttl(String key);

    void pttl(String key);
}
