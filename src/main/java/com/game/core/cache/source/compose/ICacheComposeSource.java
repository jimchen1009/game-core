package com.game.core.cache.source.compose;

import com.game.core.cache.data.IData;
import com.game.core.cache.source.executor.ICacheSource;

public interface ICacheComposeSource<K, V extends IData<K>> extends ICacheSource<K, V> {

}
