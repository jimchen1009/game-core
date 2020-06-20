package com.game.cache.source.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheRunnable implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(CacheRunnable.class);

    private final String name;
    private final Runnable runnable;

    public CacheRunnable(String name, Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        }
        catch (Throwable t){
            logger.error("{}", name, t);
        }
        finally {
        }
    }
}
