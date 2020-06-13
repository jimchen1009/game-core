package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;

import java.util.function.Consumer;

public interface ICacheDelaySource<K, V extends IData<K>> extends ICacheSource<K, V>{

    boolean flushOne(long primaryKey);

    ICacheSource<K, V> getCacheSource();

    void addFlushCallback(Consumer<PrimaryDelayCache<K, V>> consumer);
}
