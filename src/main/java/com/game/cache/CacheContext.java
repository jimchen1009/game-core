package com.game.cache;

import java.util.Date;

public class CacheContext {

    private static final ThreadLocal<CacheContext> contextStore = ThreadLocal.withInitial( ()-> new CacheContext(new Date()));

    private Date current;

    public CacheContext(Date current) {
        this.current = current;
    }

    public static void init() {
        contextStore.set(new CacheContext(new Date()));
    }

    public static void clear() {
        contextStore.remove();
    }

    public static CacheContext getContext() {
        return contextStore.get();
    }

    public static Date getCurrent(){
        return getContext().current;
    }

    public static long getCurrentTime(){
        return getContext().current.getTime();
    }
}