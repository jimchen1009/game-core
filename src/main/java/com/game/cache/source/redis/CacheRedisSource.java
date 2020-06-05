package com.game.cache.source.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.game.cache.CacheInformation;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.CacheSource;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.KeyCacheValue;
import com.game.cache.source.executor.ICacheExecutor;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * BUG 1:
 * com.game.cache.exception.CacheException: cls:com.game.cache.source.UserItem cacheValue:{"itemUniqueId":11,"count":5,"userId":1}
 *
 *
 * @param <PK>
 * @param <K>
 * @param <V>
 */
public class CacheRedisSource<PK, K, V extends IData<K>> extends CacheSource<PK, K, V> implements ICacheRedisSource<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheRedisSource.class);

    private static final SerializerFeature[] mySerializerFeatures = new SerializerFeature[] {
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.IgnoreNonFieldGetter,
            SerializerFeature.IgnoreNonFieldGetter
    };


    public CacheRedisSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        super(aClass, primaryBuilder, secondaryBuilder);
    }

    @Override
    public Map<String, Object> get(PK primaryKey, K secondaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyVString = keyValueBuilder.toSecondaryKeyString(secondaryKey);
        String string = RedisClientUtil.getRedisClient().hget(keyString, secondaryKeyVString);
        return toCacheValue(string);
    }

    @Override
    public Collection<Map<String, Object>> getAll(PK primaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> hgetAll = RedisClientUtil.getRedisClient().hgetAll(keyString);
        return toCacheValue(hgetAll.values());
    }

    @Override
    public CacheCollection getCollection(PK primaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> hgetAll = RedisClientUtil.getRedisClient().hgetAll(keyString);
        Collection<Map<String, Object>> cacheValue = toCacheValue(hgetAll.values());
        return new CacheCollection(getClassConfig().primarySharedId, cacheValue, new CacheInformation());
    }

    @Override
    public boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyString = keyValueBuilder.toSecondaryKeyString(keyCacheValue.getCacheValue());
        String jsonString = toJSONString(keyCacheValue.getCacheValue());
        RedisClientUtil.getRedisClient().hset(keyString, secondaryKeyString, jsonString);
        return true;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> jsonStringMap = keyCacheValueList.stream()
                .collect(Collectors.toMap(keyCacheValue -> keyValueBuilder.toSecondaryKeyString(keyCacheValue.getCacheValue()), keyCacheValue -> toJSONString(keyCacheValue.getCacheValue())));
        RedisClientUtil.getRedisClient().hset(keyString, jsonStringMap);
        return true;
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyString = keyValueBuilder.toSecondaryKeyString(secondaryKey);
        RedisClientUtil.getRedisClient().hdel(keyString, secondaryKeyString);
        return true;
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String[] secondaryKeyStrings = secondaryKeys.stream().map(keyValueBuilder::toSecondaryKeyString).toArray(String[]::new);
        RedisClientUtil.getRedisClient().hdel(keyString, secondaryKeyStrings);
        return true;
    }

    @Override
    public ICacheDelaySource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor) {
        throw new UnsupportedOperationException();
    }

    private String getPrimaryRedisKey(PK primaryKey){
        ClassConfig classConfig = ClassConfig.getConfig(getAClass());
        String primaryKeyString = keyValueBuilder.toPrimaryKeyString(primaryKey);
        return classConfig.getRedisKeyString(primaryKeyString);
    }

    private String toJSONString(Map<String, Object> cacheValue){
        return JSON.toJSONString(cacheValue, mySerializerFeatures);
    }

    private Map<String, Object> toCacheValue(String string){
        if (StringUtil.isEmpty(string)){
            return null;
        }
        return JSON.parseObject(string);
    }

    private Collection<Map<String, Object>> toCacheValue(Collection<String> strings){
        if (strings == null || strings.isEmpty()){
            return Collections.emptyList();
        }
        return strings.stream().map(this::toCacheValue).collect(Collectors.toList());
    }
}
