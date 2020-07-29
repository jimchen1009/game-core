package com.game.core.cache.source.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.game.core.cache.CacheInformation;
import com.game.core.cache.CacheName;
import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.DataPrivilegeUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.CacheSource;
import com.game.core.cache.source.ICacheDelaySource;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.interact.CacheRedisCollection;
import com.game.core.cache.source.interact.ICacheRedisInteract;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * 现在redis缓存使用批量加载机制, DB也实现了批量加载:
 * A没有redis数据, B 有redis数据. 但是去加载 B 的时候没有redis数据, 把 A 数据库的数据也加载上来了。
 *
 * @param <K>
 * @param <V>
 */
public class CacheRedisSource<K, V extends IData<K>> extends CacheSource<K, V> implements ICacheRedisSource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheRedisSource.class);

    private static final String ExpiredName = "ttl.ex";

    private static final SerializerFeature[] mySerializerFeatures = new SerializerFeature[] {
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.IgnoreNonFieldGetter,
            SerializerFeature.IgnoreNonFieldGetter
    };

    private final CacheInformation EMPTY_INFO = new CacheInformation();

    private final ICacheRedisInteract cacheRedisInteract;

    public CacheRedisSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheRedisInteract cacheRedisInteract) {
        super(cacheUniqueId, secondaryBuilder);
        this.cacheRedisInteract = cacheRedisInteract;
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyVString = keyValueBuilder.toSecondaryKeyString(secondaryKey);
        String string = RedisClientUtil.getRedisClient().hget(keyString, secondaryKeyVString);
        return convert2VDataValue(string);
    }

    @Override
    public List<V> getAll(long primaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> hgetAll = RedisClientUtil.getRedisClient().hgetAll(keyString);
        List<String> stringList = hgetAll.entrySet().stream().filter(entry -> !CacheName.Names.contains(entry.getKey()))
                .map( Map.Entry::getValue).collect(Collectors.toList());
        return convert2VDataValue(stringList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        ICacheUniqueId currentDaoUniqueId = getCacheUniqueId();
        CacheRedisCollection removeCollection = cacheRedisInteract.removeCollection(primaryKey, currentDaoUniqueId);
        if (removeCollection != null){
            return readDataCollection(removeCollection);
        }
        List<ICacheUniqueId> cacheDaoUniqueIdList = new ArrayList<>();
        cacheDaoUniqueIdList.add(currentDaoUniqueId);
        if (cacheRedisInteract.getAndSetSharedLoad(primaryKey, currentDaoUniqueId)){

            cacheDaoUniqueIdList.addAll(cacheRedisInteract.getSharedCacheUniqueIdList(primaryKey, currentDaoUniqueId));
        }
        List<Map.Entry<String, Object>> entryList = RedisClientUtil.getRedisClient().executeBatch(redisPipeline -> {
            for (ICacheUniqueId cacheDaoUnique : cacheDaoUniqueIdList) {
                CacheRedisCollection.executeCommand(primaryKey, redisPipeline, cacheDaoUnique);
            }
        });
        long currentTime = System.currentTimeMillis();
        Map<ICacheUniqueId, CacheRedisCollection> redisCollectionMap = new HashMap<>(cacheDaoUniqueIdList.size());
        int batchCount = entryList.size() / cacheDaoUniqueIdList.size();
        for (int i = 0; i < entryList.size(); i += batchCount) {
            CacheRedisCollection redisCollection = CacheRedisCollection.readCollection(entryList.subList(i, i + batchCount));
            if (redisCollection.isEmpty() || redisCollection.isExpired(currentTime)) {
                continue;
            }
            ICacheUniqueId cacheDaoUnique = cacheDaoUniqueIdList.get(i / batchCount);
            redisCollectionMap.put(cacheDaoUnique, redisCollection);
        }
        removeCollection = redisCollectionMap.remove(currentDaoUniqueId);
        cacheRedisInteract.addCollections(primaryKey, currentDaoUniqueId, redisCollectionMap);
        return removeCollection == null ? null : readDataCollection(removeCollection);
    }

    @Override
    public boolean replaceOne(long primaryKey, V value) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyString = keyValueBuilder.toSecondaryKeyString(value.secondaryKey());
        String jsonString = toJSONString(value);
        RedisClientUtil.getRedisClient().hset(keyString, secondaryKeyString, jsonString);
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> redisKeyValueMap = values.stream().collect(Collectors.toMap(value -> keyValueBuilder.toSecondaryKeyString(value.secondaryKey()), this::toJSONString));
        RedisClientUtil.getRedisClient().hset(keyString, redisKeyValueMap);
        return true;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.Redis;
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyString = keyValueBuilder.toSecondaryKeyString(secondaryKey);
        RedisClientUtil.getRedisClient().hdel(keyString, secondaryKeyString);
        return true;
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String[] secondaryKeyStrings = secondaryKeys.stream().map(keyValueBuilder::toSecondaryKeyString).toArray(String[]::new);
        RedisClientUtil.getRedisClient().hdel(keyString, secondaryKeyStrings);
        return true;
    }

    @Override
    public ICacheDelaySource<K, V> createDelayUpdateSource(ICacheExecutor executor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateCacheInformation(long primaryKey, CacheInformation cacheInformation) {
        String keyString = getPrimaryRedisKey(primaryKey);
        RedisClientUtil.getRedisClient().executeBatch(redisPipeline -> {
            long expiredTime = cacheInformation.getExpiredTime();
            redisPipeline.hset(keyString, CacheRedisCollection.ExpiredName, String.valueOf(expiredTime));
            redisPipeline.pexpireAt(keyString, cacheInformation.getExpiredTime());
        });
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values, CacheInformation information) {
        if (values.isEmpty() && information == CacheInformation.DEFAULT){
            return true;
        }
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> redisKeyValueMap = values.stream().collect(Collectors.toMap(value -> keyValueBuilder.toSecondaryKeyString(value.secondaryKey()), this::toJSONString));
        long expiredTime = information.getExpiredTime();
        if (expiredTime > 0) {
            redisKeyValueMap.put(ExpiredName, String.valueOf(expiredTime));
            RedisClientUtil.getRedisClient().executeBatch(redisPipeline -> {
                redisPipeline.hset(keyString, redisKeyValueMap);
                redisPipeline.pexpireAt(keyString, expiredTime);
            });
        }
        else {
            RedisClientUtil.getRedisClient().hset(keyString, redisKeyValueMap);
        }
        return true;
    }

    /**
     * 序列化成 DataCollection
     * @param redisCollection
     * @return
     */
    @SuppressWarnings("unchecked")
    private DataCollection<K, V> readDataCollection(CacheRedisCollection redisCollection){
        if (redisCollection.isEmpty()){
            return null;
        }
        List<V> dataList = convert2VDataValue(redisCollection.getRedisValues());
        return new DataCollection<>(dataList, redisCollection.getCacheInformation());
    }

    /**
     * 获取redis的键值
     * @param primaryKey
     * @return
     */
    private String getPrimaryRedisKey(long primaryKey){
        return getCacheUniqueId().getRedisKeyString(primaryKey);
    }

    /**
     * 序列化
     * @param data
     * @return
     */
    private String toJSONString(V data){
        Map<String, Object> cacheValue = getConverter().convert2Cache(data);
        cacheValue.put(CacheName.DataIndexBit.getKeyName(), data.getBitIndexBits());
        return JSON.toJSONString(cacheValue, mySerializerFeatures);
    }

    /**
     * 反序列化
     * @param string
     * @return
     */
    private V convert2VDataValue(String string){
        if (StringUtil.isEmpty(string)){
            return null;
        }
        JSONObject cacheValue = JSON.parseObject(string);
        long longValue = cacheValue.getLong(CacheName.DataIndexBit.getKeyName());
        V value = getConverter().convert2Value(cacheValue);
        DataPrivilegeUtil.invokeSetDataBitIndexBits(value, longValue);
        return value;
    }

    /**
     * 反序列化
     * @param strings
     * @return
     */
    private List<V> convert2VDataValue(Collection<String> strings){
        if (strings == null || strings.isEmpty()){
            return Collections.emptyList();
        }
        return strings.stream().map(this::convert2VDataValue).collect(Collectors.toList());
    }
}
