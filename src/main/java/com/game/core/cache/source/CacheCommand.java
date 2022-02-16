package com.game.core.cache.source;

import com.game.common.util.IEnumBase;

public enum CacheCommand implements IEnumBase {
    UPSERT(1),
    DELETE(2),
    ;
    private final int id;

    CacheCommand(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
