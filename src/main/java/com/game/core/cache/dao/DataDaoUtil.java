package com.game.core.cache.dao;

import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;

import java.util.function.Consumer;

public class DataDaoUtil {

	public static <K, V extends IData<K>> DataMapDaoBuilder<K, V> newMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder, Consumer<DataMapDaoBuilder<K, V>> consumer){
		DataMapDaoBuilder<K, V> mapDaoBuilder = DataDaoManager.getInstance().newMapDaoBuilder(aClass, secondaryBuilder);
		consumer.accept(mapDaoBuilder);
		return mapDaoBuilder;
	}

	public static <V extends IData<Long>> DataValueDaoBuilder<V> newValueDaoBuilder(Class<V> aClass, Consumer<DataValueDaoBuilder<V>> consumer){
		DataValueDaoBuilder<V> valueDaoBuilder = DataDaoManager.getInstance().newValueDaoBuilder(aClass);
		consumer.accept(valueDaoBuilder);
		return valueDaoBuilder;
	}
}
