package com.game.cache.source.mongodb;

import com.game.cache.data.IDataSource;
import com.game.cache.data.map.DataMap;

public class UserItems extends DataMap<Long, Long, UserItem> {

    public UserItems(Long primaryKey, IDataSource<Long, Long, UserItem> dataSource) {
        super(primaryKey, dataSource);
    }
}
