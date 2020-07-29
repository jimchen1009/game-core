package com.game.core.cache.data;

public interface IDataLifePredicate {

    void setOldLife(long primaryKey);

    boolean isNewLife(long primaryKey);

    IDataLifePredicate DEFAULT = new IDataLifePredicate() {
        @Override
        public void setOldLife(long primaryKey) {

        }

        @Override
        public boolean isNewLife(long primaryKey) {
            return false;
        }
    };
}
