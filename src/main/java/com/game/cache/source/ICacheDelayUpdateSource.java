package com.game.cache.source;

import com.game.cache.source.executor.ICacheSource;

import java.util.function.Consumer;

public interface ICacheDelayUpdateSource<PK, K, V> extends ICacheSource<PK, K, V>{

    void executePrimaryCacheFlushAsync(PK primaryKey, Consumer<Boolean> consumer);

    boolean executePrimaryCacheFlushSync(PK primaryKey);

    ICacheSource<PK, K, V> getCacheSource();
}
