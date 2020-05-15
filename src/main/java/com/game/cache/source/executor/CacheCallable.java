package com.game.cache.source.executor;

import com.game.cache.exception.CacheException;

import java.util.concurrent.Callable;

public class CacheCallable<V> implements Callable<V> {

    private final String name;
    private final Callable<V> callable;

    public CacheCallable(String name, Callable<V> callable) {
        this.name = name;
        this.callable = callable;
    }

    public String getName() {
        return name;
    }

    @Override
    public V call() throws Exception {
        try {
            return callable.call();
        }
        catch (Throwable t){
            throw new CacheException(name);
        }
        finally {

        }
    }
}
