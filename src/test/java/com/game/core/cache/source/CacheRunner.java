package com.game.core.cache.source;

import com.game.core.cache.ClassConfig;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.dao.DataDaoManager;
import com.game.core.cache.dao.DataDaoUtil;
import com.game.core.cache.dao.IDataCacheMapDao;
import com.game.core.cache.dao.IDataMapDao;
import com.game.core.cache.data.IDataLifePredicate;
import com.game.core.cache.key.KeyValueHelper;
import com.game.core.cache.source.interact.ICacheLifeInteract;
import com.game.core.cache.source.redis.RedisClientUtil;
import com.game.core.db.mongodb.MongoDbManager;
import com.game.core.db.redis.IRedisClient;
import com.game.core.db.redis.RedisClientManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jodd.util.ThreadUtil;
import org.bson.Document;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheRunner {

    private static final Logger logger = LoggerFactory.getLogger(CacheRunner.class);


    @Test
    public void item() throws InterruptedException {
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

        IDataLifePredicate lifePredicate = new IDataLifePredicate() {
            @Override
            public void setOldLife(long primaryKey) {
            }

            @Override
            public boolean isNewLife(long primaryKey) {
                return false;
            }
        };

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        ICacheLifeInteract loginPredicate = new ICacheLifeInteract() {

            private final Map<Long, AtomicBoolean> atomicBoolean0 = new ConcurrentHashMap<>();
            @Override
            public boolean getAndSetSharedLoad(long primaryKey, ICacheUniqueId cacheDaoUnique) {
                return atomicBoolean0.computeIfAbsent(primaryKey, key -> new AtomicBoolean(false)).compareAndSet(false, true);
            }
        };

        IDataCacheMapDao<Long, UserItem> itemDao = DataDaoUtil.newMapDaoBuilder(UserItem.class, KeyValueHelper.LongBuilder, builder -> {
            builder.setCacheLoginPredicate(loginPredicate);
            builder.setLifePredicate(lifePredicate);
            ClassConfig classConfig = builder.getClassConfig();
            classConfig.setDelayUpdate(true)
                    .setName("material")
                    .setPrimarySharedId(1)
                    .setRedisSupport(true)
                    .setCacheLoadAdvance(true)
                    .setAccountCache(true);
        }).createIfAbsent();


        IDataMapDao<Integer, UserCurrency> currencyDao = DataDaoUtil.newMapDaoBuilder(UserCurrency.class, KeyValueHelper.IntegerBuilder, builder -> {
            builder.setCacheLoginPredicate(loginPredicate);
            builder.setLifePredicate(lifePredicate);
            ClassConfig classConfig = builder.getClassConfig();
            classConfig.setDelayUpdate(true)
                    .setName("material")
                    .setPrimarySharedId(2)
                    .setRedisSupport(true)
                    .setCacheLoadAdvance(true)
                    .setAccountCache(true);
        }).createIfAbsent();


        List<UserDaoAll> userDaoAllList = new ArrayList<>();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        for (long userId = 1L; userId <= 1; userId++) {

            userDaoAllList.add(new UserDaoAll(userId, itemDao, currencyDao));
        }
        userDaoAllList.get(0).execute();

//        for (int i = 0; i < 2; i++) {
//            executorService.scheduleAtFixedRate(()-> {
//                int index = RandomUtils.nextInt(0, userDaoAllList.size());
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
