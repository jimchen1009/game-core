package com.game.core.cache.data;

import com.game.core.cache.ICacheUniqueId;

public interface IDataLifePredicate {

    boolean withoutUpdate(long primaryKey, ICacheUniqueId cacheUniqueId);

    void doneUpdate(long primaryKey, ICacheUniqueId cacheUniqueId);

    default boolean compareAndUpdate(long primaryKey, ICacheUniqueId cacheUniqueId){
        boolean withoutUpdate = withoutUpdate(primaryKey, cacheUniqueId);
        if (withoutUpdate){
            doneUpdate(primaryKey, cacheUniqueId);
        }
        return withoutUpdate;
    }

    IDataLifePredicate DEFAULT = new IDataLifePredicate() {

        @Override
        public boolean withoutUpdate(long primaryKey, ICacheUniqueId cacheUniqueId) {
            return false;
        }

        @Override
        public void doneUpdate(long primaryKey, ICacheUniqueId cacheUniqueId) {

        }
    };
}
