package com.game.core.cache.exception;

public class CacheException extends RuntimeException {

    public CacheException(String string, Object... objects) {
        super(objects.length == 0 ? string : String.format(string, objects));
    }

    public CacheException(String string, Throwable exception, Object... objects) {
        super(objects.length == 0 ? string : String.format(string, objects), exception);
    }
}
