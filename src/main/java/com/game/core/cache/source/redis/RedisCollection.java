package com.game.core.cache.source.redis;

import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.IData;

import java.util.List;

public class RedisCollection<K, V extends IData<K>> extends DataCollection<K, V>  {

	private final RedisInformation redisInformation;

	public RedisCollection(List<V> dataList, RedisInformation redisInformation) {
		super(dataList);
		this.redisInformation = redisInformation;
	}

	public RedisInformation getRedisInformation() {
		return redisInformation;
	}

	public boolean isExpired(long currentTime){
		return redisInformation.isExpired(currentTime);
	}
}
