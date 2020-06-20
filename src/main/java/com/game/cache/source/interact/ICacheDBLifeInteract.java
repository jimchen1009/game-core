package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueKey;

public interface ICacheDBLifeInteract extends ICacheLifeInteract {

    boolean dbGetAndSetSharedLoad(long primaryKey, ICacheUniqueKey cacheDaoUnique);

    ICacheDBLifeInteract DEFAULT = new ICacheDBLifeInteract() {

        @Override
        public boolean dbGetAndSetSharedLoad(long primaryKey, ICacheUniqueKey cacheDaoUnique) {
            return false;
        }

        @Override
        public boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueKey cacheDaoUnique) {
            return false;
        }
    };
}
