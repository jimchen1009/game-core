package com.game.cache.source;

import com.game.cache.dao.DataDaoManager;
import com.game.cache.dao.IDataMapDao;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.key.KeyValueHelper;
import com.game.cache.source.redis.RedisClientUtil;
import com.game.db.mongodb.MongoDbManager;
import com.game.db.redis.IRedisClient;
import com.game.db.redis.RedisClientManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jodd.util.ThreadUtil;
import org.apache.commons.lang3.RandomUtils;
import org.bson.Document;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheRunner {

    private static final Logger logger = LoggerFactory.getLogger(CacheRunner.class);


    @Test
    public void item(){
        MongoDbManager.init();
        RedisClientManager.init();
        IRedisClient redisClient = RedisClientUtil.getRedisClient();
        MongoDatabase database = MongoDbManager.get("cache").getDb("demo");
        MongoCollection<Document> collection = database.getCollection("material");
//        collection.drop();
//        Document queryDocument = new Document("userId", new Document("$exists", false));
//        Document document = new Document("$replace", new Document("item", Collections.emptyList()));
//        collection.updateMany(queryDocument, document);

//        collection.insertOne(new Document("userId", userId).append("item", Collections.emptyList()));

        List<UserDaoAll> userDaoAllList = new ArrayList<>();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        for (long userId = 1L; userId <= 2000; userId++) {
            IDataLoadPredicate<Long> loadPredicate = new IDataLoadPredicate<Long>() {
                @Override
                public void onPredicateCacheLoaded(Long primaryKey) {
                }

                @Override
                public boolean predicateNoCache(Long primaryKey) {
                    return false;
                }
            };

            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            ICacheLoginPredicate<Long> loginPredicate = new ICacheLoginPredicate<Long>() {

                private final AtomicBoolean atomicBoolean0 = new AtomicBoolean(false);
                private final AtomicBoolean atomicBoolean1 = new AtomicBoolean(false);

                @Override
                public boolean loginSharedLoadTable(Long primaryKey, String tableName) {
                    return atomicBoolean0.compareAndSet(false, true);
                }

                @Override
                public boolean loginSharedLoadRedis(Long primaryKey, int redisSharedId) {
                    return atomicBoolean1.compareAndSet(false, true);
                }
            };

            IDataMapDao<Long, Long, UserItem> itemDao = DataDaoManager.getInstance()
                    .newDataMapDaoBuilder(UserItem.class, KeyValueHelper.LongBuilder, KeyValueHelper.LongBuilder)
                    .setCacheLoginPredicate(loginPredicate)
                    .setLoadPredicate(loadPredicate).buildCache();
            IDataMapDao<Long, Integer, UserCurrency> currencyDao = DataDaoManager.getInstance()
                    .newDataMapDaoBuilder(UserCurrency.class, KeyValueHelper.LongBuilder, KeyValueHelper.IntegerBuilder)
                    .setCacheLoginPredicate(loginPredicate)
                    .setLoadPredicate(loadPredicate).buildCache();

            userDaoAllList.add(new UserDaoAll(userId, itemDao, currencyDao));
        }

        for (int i = 0; i < 100; i++) {
            executorService.scheduleAtFixedRate(()-> {
                int index = RandomUtils.nextInt(0, userDaoAllList.size());
                try {
                    userDaoAllList.get(index).execute();
                }
                catch (Throwable t){
//                    logger.error("", t);
                }
            }, 10, 500, TimeUnit.MILLISECONDS);
        }

        ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(30));

        executorService.shutdownNow();
        ThreadUtil.sleep(2000L);
        DataDaoManager.getInstance().flushAll();


//        itemDao.deleteBatch(userId, updateItemList.stream().map(UserItem::secondaryKey).collect(Collectors.toList()));
//        currencyDao.deleteBatch(userId, updateCurrencyList.stream().map(UserCurrency::secondaryKey).collect(Collectors.toList()));
//        ThreadUtil.sleep(TimeUnit.MINUTES.toMillis(2));
    }

    private static class UserDaoAll{

        private final long userId;
        private final IDataMapDao<Long, Long, UserItem> itemDao;
        private final IDataMapDao<Long, Integer, UserCurrency> currencyDao;

        public UserDaoAll(long userId, IDataMapDao<Long, Long, UserItem> itemDao, IDataMapDao<Long, Integer, UserCurrency> currencyDao) {
            this.userId = userId;
            this.itemDao = itemDao;
            this.currencyDao = currencyDao;
        }

        public void execute(){
            itemDao.getAll(userId);
            currencyDao.getAll(userId);

            Collection<UserItem> updateItemList = new ArrayList<>();
            Collection<UserCurrency> updateCurrencyList = new ArrayList<>();

            for (int i = 1; i <= 200; i++) {
                UserItem userItem = new UserItem(userId, i, 10);
                updateItemList.add(userItem);
                UserCurrency userCurrency = new UserCurrency(userId, i, 10);
                updateCurrencyList.add(userCurrency);
            }
            itemDao.replaceBatch(userId, updateItemList);
            currencyDao.replaceBatch(userId, updateCurrencyList);

            for (int i = 0; i < 200; i++) {
                updateItemList.forEach(userItem -> userItem.decCount(1));
                itemDao.replaceBatch(userId, updateItemList);

                updateCurrencyList.forEach(userCurrency -> userCurrency.decCount(1));
                currencyDao.replaceBatch(userId, updateCurrencyList);
            }
            itemDao.getAll(userId);
            currencyDao.getAll(userId);
        }
    }

    @Test
    public void player(){

    }
}
