package com.game.db.redis;

public class RedisException extends RuntimeException {

    public RedisException(String string, Object... objects) {
        super(objects.length == 0 ? string : String.format(string, objects));
    }

    public RedisException(String string, Throwable exception, Object... objects) {
        super(objects.length == 0 ? string : String.format(string, objects), exception);
    }
}
