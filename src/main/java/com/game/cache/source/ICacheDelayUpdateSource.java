package com.game.cache.source;

import java.util.function.Consumer;

public interface ICacheDelayUpdateSource<PK> {

    void executePrimaryCacheWriteBack(PK primaryKey, Consumer<Boolean> consumer);
}
