package com.game.cache.source.compose;

import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;

public interface ICacheComposeSource<PK, K, V extends IData<K>> extends ICacheSource<PK, K, V> {

}
