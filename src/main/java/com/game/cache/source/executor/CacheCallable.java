package com.game.cache.source.executor;

import com.game.cache.exception.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class CacheCallable<V> implements Callable<V> {


    private static final Logger logger = LoggerFactory.getLogger(CacheCallable.class);

    private final String name;
    private final Callable<V> callable;
    private final Consumer<V> consumer;

    public CacheCallable(String name, Callable<V> callable, Consumer<V> consumer) {
        this.name = name;
        this.callable = callable;
        this.consumer = consumer;
    }

    public String getName() {
        return name;
    }

    @Override
    public V call() throws Exception {
        V value;
        try {
            value = callable.call();
            if (consumer != null){
                consumer.accept(value);
            }
            logger.trace("callable:{} call success.", getName());
            return value;
        }
        catch (Throwable t){
            throw new CacheException(name);
        }
        finally {
        }
    }
}
