package com.game.cache.source.mongodb;

import com.game.cache.dao.DataDaoManager;
import com.game.cache.dao.IDataCacheMapDao;
import com.game.cache.data.Data;
import com.game.cache.data.DataSource;
import com.game.cache.data.map.DataMapContainer;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.key.KeyValueHelper;
import com.game.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.cache.source.executor.CacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.db.mongodb.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jodd.util.ThreadUtil;
import org.bson.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CacheRunner {

    @Test
    public void item(){
        MongoDatabase database = MongoDBManager.getInstance().getDb("demo");
        MongoCollection<Document> collection = database.getCollection("material");
//        collection.drop();
        long userId = 1L;
//        Document queryDocument = new Document("userId", new Document("$exists", false));
//        Document document = new Document("$replace", new Document("item", Collections.emptyList()));
//        collection.updateMany(queryDocument, document);

//        collection.insertOne(new Document("userId", userId).append("item", Collections.emptyList()));

        IDataCacheMapDao<Long, Long, UserItem> itemDao = DataDaoManager.getInstance().getCacheMapDao(UserItem.class, KeyValueHelper.LongBuilder, KeyValueHelper.LongBuilder);
        IDataCacheMapDao<Long, Integer, UserCurrency> currencyDao = DataDaoManager.getInstance().getCacheMapDao(UserCurrency.class, KeyValueHelper.LongBuilder, KeyValueHelper.IntegerBuilder);

        Collection<UserItem> updateItemList = new ArrayList<>();
        Collection<UserCurrency> updateCurrencyList = new ArrayList<>();

        for (int i = 1; i <= 15; i++) {
            UserItem userItem = new UserItem(userId, i, 10);
            updateItemList.add(userItem);
            UserCurrency userCurrency = new UserCurrency(userId, i, 10);
            updateCurrencyList.add(userCurrency);
        }
        itemDao.replaceBatch(userId, updateItemList);
        currencyDao.replaceBatch(userId, updateCurrencyList);

        for (int i = 0; i < 5; i++) {
            updateItemList.forEach(userItem -> userItem.decCount(1));
            itemDao.replaceBatch(userId, updateItemList);

            updateCurrencyList.forEach(userCurrency -> userCurrency.decCount(1));
            currencyDao.replaceBatch(userId, updateCurrencyList);
        }

        ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(10));

        itemDao.deleteBatch(userId, updateItemList.stream().map(UserItem::secondaryKey).collect(Collectors.toList()));
        currencyDao.deleteBatch(userId, updateCurrencyList.stream().map(UserCurrency::secondaryKey).collect(Collectors.toList()));
        ThreadUtil.sleep(TimeUnit.MINUTES.toMillis(2));
    }

    private <PK, K, V extends Data<K>> DataMapContainer<PK, K, V> createMapContainer(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder,
                                                                                     CacheExecutor executor){
        ICacheSource<PK, K, V> cacheSource = new CacheDirectMongoDBSource<>(aClass, primaryBuilder, secondaryBuilder);
        cacheSource = cacheSource.createDelayUpdateSource(executor);
        MongoDBConvertMapper convertMapper = new MongoDBConvertMapper();
        DataSource<PK, K, V> dataSource = new DataSource<>(aClass, convertMapper, cacheSource);
        DataMapContainer<PK, K, V> mapContainer = new DataMapContainer<>(dataSource);
        return mapContainer;
    }

    @Test
    public void player(){

    }
}
