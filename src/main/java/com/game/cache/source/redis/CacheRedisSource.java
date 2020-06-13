package com.game.cache.source.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.game.cache.CacheDaoUnique;
import com.game.cache.CacheInformation;
import com.game.cache.CacheName;
import com.game.cache.CacheType;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.JsonValueConverter;
import com.game.cache.source.CacheSource;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.executor.ICacheExecutor;
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
 * BUG 1:
 * com.game.cache.exception.CacheException: cls:com.game.cache.source.UserItem cacheValue:{"itemUniqueId":11,"count":5,"userId":1}
 *
 *
 * @param <K>
 * @param <V>
 */
public class CacheRedisSource<K, V extends IData<K>> extends CacheSource<K, V> implements ICacheRedisSource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheRedisSource.class);

    private static final SerializerFeature[] mySerializerFeatures = new SerializerFeature[] {
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.IgnoreNonFieldGetter,
            SerializerFeature.IgnoreNonFieldGetter
    };

    public CacheRedisSource(CacheDaoUnique cacheDaoKey, IKeyValueBuilder<K> secondaryBuilder) {
        super(cacheDaoKey, secondaryBuilder);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.Redis;
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyVString = keyValueBuilder.toSecondaryKeyString(secondaryKey);
        String string = RedisClientUtil.getRedisClient().hget(keyString, secondaryKeyVString);
        return parseDataValue(string);
    }

    @Override
    public List<V> getAll(long primaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> hgetAll = RedisClientUtil.getRedisClient().hgetAll(keyString);
        List<String> stringList = hgetAll.entrySet().stream().filter(entry -> !CacheName.Names.contains(entry.getKey()))
                .map( Map.Entry::getValue).collect(Collectors.toList());
        return parseDataValue(stringList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        //还有BUG
//        List<Object> objectList = RedisClientUtil.getRedisClient().pipeline(pipeline -> {
//            pipeline.hgetAll(keyString);
//            pipeline.pttl(keyString);
//        });
//        Map<String, String> strings = (Map<String, String>)objectList.get(0);
//        long ttl = (Long)objectList.get(1);
        Map<String, String> strings = RedisClientUtil.getRedisClient().hgetAll(keyString);
        List<String> stringList = new ArrayList<>();
        CacheInformation cacheInformation = new CacheInformation();
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String> entry : strings.entrySet()) {
            if (CacheName.Names.contains(entry.getKey())) {
                Object object = JsonValueConverter.parse(entry.getValue());
                map.put(entry.getKey(), object);
            }
            else {
                stringList.add(entry.getValue());
            }
        }
        List<V> dataList = parseDataValue(stringList);
        return new DataCollection(dataList, new CacheInformation(map));
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
        return replaceBatch(primaryKey, values, null);
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

    private String getPrimaryRedisKey(long primaryKey){
        return getCacheDaoUnique().getRedisKeyString(primaryKey);
    }

    private String toJSONString(V data){
        Map<String, Object> cacheValue = getConverter().convert2Cache(data);
        return JSON.toJSONString(cacheValue, mySerializerFeatures);
    }

    private V parseDataValue(String string){
        if (StringUtil.isEmpty(string)){
            return null;
        }
        JSONObject cacheValue = JSON.parseObject(string);
        return getConverter().convert2Value(cacheValue);
    }

    private List<V> parseDataValue(Collection<String> strings){
        if (strings == null || strings.isEmpty()){
            return Collections.emptyList();
        }
        return strings.stream().map(this::parseDataValue).collect(Collectors.toList());
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values, CacheInformation information) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> jsonStringMap = values.stream().collect(Collectors.toMap(value -> keyValueBuilder.toSecondaryKeyString(value.secondaryKey()), this::toJSONString));
        if (information != null){
            for (Map.Entry<String, Object> entry : information.entrySet()) {
                jsonStringMap.put(entry.getKey(), JsonValueConverter.toJSONString(entry.getValue()));
            }
        }
        RedisClientUtil.getRedisClient().hset(keyString, jsonStringMap);
        return true;
    }
}
