package com.game.core.cache.source.executor;

import com.game.common.concurrent.QueueJob;
import com.game.core.cache.exception.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class CacheCallable<V> extends QueueJob<String> {

    private static final Logger logger = LoggerFactory.getLogger(CacheCallable.class);

    private final Callable<V> callable;
    private final Consumer<V> consumer;

    public CacheCallable(String cacheName, String message, Callable<V> callable, Consumer<V> consumer) {
        super(cacheName, message);
        this.callable = callable;
        this.consumer = consumer;
    }

    @Override
    protected void execute() {
        V value = null;
        try {
            value = callable.call();
            logger.trace("callable:{} call success.", getMessage());
        }
        catch (Throwable t){
            throw new CacheException(getMessage());
        }
        finally {
            try {
                if (consumer != null){
                    consumer.accept(value);
                }
            }
            catch (Throwable t){
                logger.error("callable:{} call error.", getMessage());
            }
        }
    }
}
