package com.game.core.cache.source;

import com.game.core.cache.data.IData;
import com.game.core.cache.source.executor.ICacheSource;

import java.util.function.Consumer;

public interface ICacheDelaySource<K, V extends IData<K>> extends ICacheSource<K, V>{

    ICacheSource<K, V> getCacheSource();

    void setWriteBackCallback(Consumer<PrimaryDelayCache<K, V>> consumer);
}
