package com.game.core.cache.source.interact;

import com.game.core.cache.ICacheUniqueId;

public interface ICacheLifeInteract {

    boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueId cacheDaoUnique);

    ICacheLifeInteract DEFAULT = (primaryKey, cacheDaoUnique) -> false;
}
