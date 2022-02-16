package com.game.core.cache.source.compose;

import com.game.core.cache.data.IData;
import com.game.core.cache.source.executor.ICacheSource;

import java.util.function.BiFunction;

public interface IRedisDbSource<K, V extends IData<K>> extends ICacheSource<K, V> {

	void setGetDataFunction(BiFunction<Long, K, V> function);
}
