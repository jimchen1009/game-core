package com.game.cache.source.executor;

public class CacheRunnable implements Runnable{

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

        }
        finally {

        }
    }
}
