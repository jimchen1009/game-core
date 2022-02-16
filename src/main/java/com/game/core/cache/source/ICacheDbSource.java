package com.game.core.cache.source;

import com.game.core.cache.data.IData;
import com.game.core.cache.source.executor.ICacheSource;

public interface ICacheDbSource<K, V extends IData<K>> extends ICacheSource<K, V> {
}
