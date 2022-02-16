package com.game.core.cache.source.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;
import com.game.common.util.RandomUtil;
import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.CacheSource;
import com.game.core.cache.source.executor.CacheRunnable;
import com.game.core.cache.source.executor.CacheSourceUtil;
import com.game.core.cache.source.executor.ICacheExecutor;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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

    private static final String TTL_EXPIRED = "#core.ttl#";
    private static final String DATA_BITS = "#core.bits#";

    private static final SerializerFeature[] mySerializerFeatures = new SerializerFeature[] {
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.IgnoreNonFieldGetter,
            SerializerFeature.IgnoreNonFieldGetter
    };

    private final Map<Long, RedisInformation> informationMap;

    public CacheRedisSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheExecutor executor) {
        super(cacheUniqueId, secondaryBuilder, executor);
        this.informationMap = new ConcurrentHashMap<>();
        long initialDelay = RandomUtil.nextLong(1000, 2000) / 50;
        executor.scheduleWithFixedDelay(new CacheRunnable("RedisJob." + getCacheUniqueId().getName(), this::onScheduleAll), initialDelay, 1000L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        informationMap.remove(primaryKey);
        consumer.accept(true);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyVString = keyValueBuilder.toSecondaryKeyString(secondaryKey);
        String string = RedisUtil.getClient().hget(keyString, secondaryKeyVString);
        return convert2VDataValue(string);
    }

    @Override
    public List<V> getAll(long primaryKey) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> hgetAll = RedisUtil.getClient().hgetAll(keyString);
        hgetAll.remove(TTL_EXPIRED);
        return convert2VDataValue(hgetAll.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public RedisCollection<K, V> getCollection(long primaryKey) {
        String redisKeyString = getCacheUniqueId().getRedisKeyString(primaryKey);
        Map<String, String> redisKeyValueMap = RedisUtil.getClient().hgetAll(redisKeyString);
        boolean emptyCache = redisKeyValueMap.isEmpty();
        RedisInformation redisInformation;
        String expiredString = redisKeyValueMap.remove(TTL_EXPIRED);
        if (expiredString != null){
            TTL ttl = TTL.readRedisString(expiredString);
            redisInformation = new RedisInformation(ttl.expiredTime, TTL.getUpdateInAdvance(getCacheUniqueId()));
        }
        else if (emptyCache){
            redisInformation = new RedisInformation(1, TTL.getUpdateInAdvance(getCacheUniqueId()));
        }
        else {
            redisInformation = new RedisInformation();
        }
        List<V> dataList = convert2VDataValue(redisKeyValueMap.values());
        informationMap.put(primaryKey, redisInformation.cloneInformation());
        return new RedisCollection<>(dataList, redisInformation);
    }

    @Override
    public boolean replaceOne(long primaryKey, V data) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String secondaryKeyString = keyValueBuilder.toSecondaryKeyString(data.secondaryKey());
        String jsonString = toJSONString(data);
        RedisUtil.getClient().hset(keyString, secondaryKeyString, jsonString);
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> dataList) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> redisKeyValueMap = dataList.stream().collect(Collectors.toMap(value -> keyValueBuilder.toSecondaryKeyString(value.secondaryKey()), this::toJSONString));
        RedisUtil.getClient().hset(keyString, redisKeyValueMap);
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
        RedisUtil.getClient().hdel(keyString, secondaryKeyString);
        return true;
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        String keyString = getPrimaryRedisKey(primaryKey);
        String[] secondaryKeyStrings = secondaryKeys.stream().map(keyValueBuilder::toSecondaryKeyString).toArray(String[]::new);
        RedisUtil.getClient().hdel(keyString, secondaryKeyStrings);
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, DataCollection<K, V> dataCollection) {
        String keyString = getPrimaryRedisKey(primaryKey);
        Map<String, String> redisKeyValueMap = dataCollection.getDataList()
                .stream()
                .collect(Collectors.toMap(value -> keyValueBuilder.toSecondaryKeyString(value.secondaryKey()), this::toJSONString));
        TTL ttl = TTL.createTTL(getCacheUniqueId());
        redisKeyValueMap.put(TTL_EXPIRED, ttl.toExpiredString());
        RedisUtil.getClient().executeBatch( pipeline -> {
            pipeline.del(keyString);
            pipeline.hset(keyString, redisKeyValueMap);
            pipeline.pexpireAt(keyString, ttl.ttlExpiredTime);
        });
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<K> secondaryKeys, Collection<V> dataList) {
        if (secondaryKeys.isEmpty() && dataList.isEmpty()){
            return true;
        }
        if (secondaryKeys.isEmpty()) {
            return replaceBatch(primaryKey, dataList);
        }
        else if (dataList.isEmpty()){
            return deleteBatch(primaryKey, secondaryKeys);
        }
        else {
            String keyString = getPrimaryRedisKey(primaryKey);
            Map<String, String> redisKeyValueMap = dataList.stream().collect(Collectors.toMap(value -> keyValueBuilder.toSecondaryKeyString(value.secondaryKey()), this::toJSONString));
            String[] secondaryKeyStrings = secondaryKeys.stream().map(keyValueBuilder::toSecondaryKeyString).toArray(String[]::new);
            RedisUtil.getClient().executeBatch( pipeline -> {
                pipeline.hset(keyString, redisKeyValueMap);
                pipeline.hdel(keyString, secondaryKeyStrings);
            });
            return true;
        }
    }

    private void onScheduleAll(){
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Long, RedisInformation> entry : informationMap.entrySet()) {
            Long primaryKey = entry.getKey();
            try {
                if (!entry.getValue().needUpdateExpired(currentTime)){
                    return;
                }
                // TODO 暂时单个处理, 是否可能替换成批处理
                CacheSourceUtil.submitCallable(this, "onScheduleAll", () -> {
                    RedisInformation information = informationMap.get(primaryKey);
                    return information == null || updateInformation(primaryKey, information);
                }, null);
            }
            catch (Throwable t){
                logger.error("primaryKey:{} onScheduleAll error.", primaryKey);
            }
        }
    }

    private boolean updateInformation(long primaryKey, RedisInformation redisInformation) {
        String keyString = getPrimaryRedisKey(primaryKey);
        // 如果恰好数据过期没有设置成功, 就默认不处理, 直接让上层数据回写
        TTL ttl = TTL.createTTL(getCacheUniqueId());
        Long aLong = RedisUtil.getClient().expireAt(keyString, ttl.ttlExpiredTime);
        boolean isSuccess = aLong == 1;
        if (isSuccess){
            RedisUtil.getClient().hset(keyString, TTL_EXPIRED, ttl.toExpiredString());
            redisInformation.updateExpiredTime(ttl.expiredTime, TTL.getUpdateInAdvance(getCacheUniqueId()));
        }
        return isSuccess;
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
        return getConverter().convert2Data(cacheValue);
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

    private static class TTL {

        /** 这个值定义之后就不能修改, 除非把redis删掉或者改掉版本号 */
        private static final long MIN = TimeUnit.DAYS.toMillis(2);
        private static final long MAX = TimeUnit.DAYS.toMillis(4);

        private final long expiredTime;
        private final long ttlExpiredTime;

        public TTL(long expiredTime, long ttlExpiredTime) {
            this.expiredTime = expiredTime;
            this.ttlExpiredTime = ttlExpiredTime;
        }

        public String toExpiredString(){
            return String.format("%s_%s", expiredTime, ttlExpiredTime);
        }

        public static TTL readRedisString(String expiredString){
            String[] strings = expiredString.split("_");
            return new TTL(Long.parseLong(strings[0]), Long.parseLong(strings[1]));
        }

        public static TTL createTTL(ICacheUniqueId cacheUniqueId){
            long currentTime = System.currentTimeMillis();
            long redisDuration = cacheUniqueId.getRedisDuration();
            if (redisDuration > 0){
                redisDuration = Math.max(redisDuration, EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getDuration("redis.db.lifeDuration", TimeUnit.MILLISECONDS));
            }
            long expiredTime = currentTime + redisDuration;
            return new TTL(expiredTime, expiredTime + RandomUtil.nextLong(MIN, MAX));
        }

        public static long getUpdateInAdvance(ICacheUniqueId cacheUniqueId){
            return Math.max(0, cacheUniqueId.getRedisDuration() / 2);
        }
    }
}
