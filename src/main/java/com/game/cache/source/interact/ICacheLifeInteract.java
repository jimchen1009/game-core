package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueId;

public interface ICacheLifeInteract {

    boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueId cacheDaoUnique);

    ICacheLifeInteract DEFAULT = (primaryKey, cacheDaoUnique) -> false;
}
