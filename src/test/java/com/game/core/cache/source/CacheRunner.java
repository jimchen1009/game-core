package com.game.core.cache.source;

import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.dao.DataDaoManager;
import com.game.core.cache.dao.DataDaoUtil;
import com.game.core.cache.dao.IDataCacheMapDao;
import com.game.core.cache.dao.IDataMapDao;
import com.game.core.cache.data.IDataLifePredicate;
import com.game.core.cache.key.KeyValueBuilder;
import com.game.core.db.sql.SqlDbs;
import jodd.util.ThreadUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CacheRunner {

    private static final Logger logger = LoggerFactory.getLogger(CacheRunner.class);


    @Test
    public void item() throws InterruptedException {
        System.setProperty("game.core.config.path", "D:/demo/game-core/src/main/resources");
//        MongoDbManager.initialize();
//        RedisClientManager.initialize();
//        IRedisClient redisClient = RedisUtil.getClient();
//        MongoDatabase database = MongoDbManager.get("cache").getDb("demo");
//        MongoCollection<Document> collection = database.getCollection("material");
//        collection.drop();
//        Document queryDocument = new Document("userId", new Document("$exists", false));
//        Document document = new Document("$replace", new Document("item", Collections.emptyList()));
//        collection.updateMany(queryDocument, document);

//        collection.insertOne(new Document("userId", userId).append("item", Collections.emptyList()));

        List<Object> collect = Collections.singletonList(1).stream().map(a -> null).collect(Collectors.toList());
        SqlDbs.initialize();
        IDataLifePredicate lifePredicate = new IDataLifePredicate() {

            @Override
            public boolean withoutUpdate(long primaryKey, ICacheUniqueId cacheUniqueId) {
                return false;
            }

            @Override
            public void doneUpdate(long primaryKey, ICacheUniqueId cacheUniqueId) {

            }
        };

        IDataCacheMapDao<Long, UserItem> itemDao = DataDaoUtil.newMapDaoBuilder(UserItem.class, new KeyValueBuilder.ONE<>(), builder -> {
            builder.setLifePredicate(lifePredicate);
            builder.getClassConfig().setDelayUpdate(false)
                    .setName("item")
                    .setRedisSupport(false)
                    .setCacheLoadAdvance(true)
                    .setAccountCache(true)
                    .setCacheType(CacheType.MySQL);
        }).getCacheInstance();


        IDataMapDao<Integer, UserCurrency> currencyDao = DataDaoUtil.newMapDaoBuilder(UserCurrency.class, new KeyValueBuilder.ONE<>(), builder -> {
            builder.setLifePredicate(lifePredicate);
            builder.getClassConfig().setDelayUpdate(true)
                    .setName("currency")
                    .setRedisSupport(true)
                    .setCacheLoadAdvance(true)
                    .setAccountCache(true)
                    .setCacheType(CacheType.MySQL);
        }).getCacheInstance();


        List<UserDaoAll> userDaoAllList = new ArrayList<>();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        for (long userId = 1L; userId <= 1; userId++) {

            userDaoAllList.add(new UserDaoAll(userId, itemDao, currencyDao));
        }
        userDaoAllList.get(0).execute();

//        for (int i = 0; i < 2; i++) {
//            executorService.scheduleWithFixedDelay(()-> {
//                int index = RandomUtils.select(0, userDaoAllList.size());
//                try {
//                    userDaoAllList.get(index).execute();
//                }
//                catch (Throwable t){
////                    logger.error("", t);
//                }
//            }, 10, 500, TimeUnit.MILLISECONDS);
//        }

        ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(5));

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        for (UserDaoAll all : userDaoAllList) {
            DataDaoManager.getInstance().flushUserAll(all.userId, success -> System.out.println(all.userId + " 回写:" + success));
        }
        ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(10));

        DataDaoManager.getInstance().flushAll();


//        itemDao.deleteBatch(userId, updateItemList.stream().map(UserItem::secondaryKey).collect(Collectors.toList()));
//        currencyDao.deleteBatch(userId, updateCurrencyList.stream().map(UserCurrency::secondaryKey).collect(Collectors.toList()));
        ThreadUtil.sleep(TimeUnit.MINUTES.toMillis(2));
    }

    private static class UserDaoAll{

        private final long userId;
        private final IDataMapDao<Long, UserItem> itemDao;
        private final IDataMapDao<Integer, UserCurrency> currencyDao;

        public UserDaoAll(long userId, IDataMapDao<Long, UserItem> itemDao, IDataMapDao<Integer, UserCurrency> currencyDao) {
            this.userId = userId;
            this.itemDao = itemDao;
            this.currencyDao = currencyDao;
        }

        public void execute(){
            itemDao.getAll(userId);
            currencyDao.getAll(userId);

            Collection<UserItem> updateItemList = new ArrayList<>();
            Collection<UserCurrency> updateCurrencyList = new ArrayList<>();

            for (int i = 1; i <= 2; i++) {
                UserItem userItem = new UserItem(userId, i, 10);
                updateItemList.add(userItem);
                UserCurrency userCurrency = new UserCurrency(userId, i, 10);
                updateCurrencyList.add(userCurrency);
            }
//            itemDao.replaceBatch(userId, updateItemList);
            currencyDao.replaceBatch(userId, updateCurrencyList);

            for (int i = 0; i < 2; i++) {
                updateItemList.forEach(userItem -> userItem.decCount(1));
                itemDao.replaceBatch(userId, updateItemList);

//                updateCurrencyList.forEach(userCurrency -> userCurrency.decCount(1));
//                currencyDao.replaceBatch(userId, updateCurrencyList);
            }
            itemDao.getAll(userId);
            currencyDao.getAll(userId);
        }
    }

    @Test
    public void player(){

    }
}
