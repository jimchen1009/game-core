package com.game.core.db.redis;

import java.util.Map;

public interface IRedisPipeline {

    void hgetAll(String key);

    void hset(String key, String field, String value);

    void hset(final String key, final Map<String, String> hash);

    void ttl(String key);

    void pttl(String key);

    void pexpireAt(final String key, final long millisecondsTimestamp);

    void del(String key);

    void hdel(final String key, final String... field);

    int commandCount();
}
