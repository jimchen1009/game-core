package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueKey;

public interface ICacheLifeInteract {

    boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueKey cacheDaoUnique);
}
