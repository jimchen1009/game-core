package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;

import java.util.function.Consumer;

public interface ICacheDelaySource<PK, K, V extends IData<K>> extends ICacheSource<PK, K, V>{

    void flush(PK primaryKey, Consumer<Boolean> consumer);

    boolean flush(PK primaryKey);

    ICacheSource<PK, K, V> getCacheSource();
}
