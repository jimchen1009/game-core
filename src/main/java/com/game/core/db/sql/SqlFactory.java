package com.game.core.db.sql;

import com.game.common.config.IEvnConfig;

public enum SqlFactory {
	HikariCP(HikariCPDataSource.Builder.class),
	;

	private Class<? extends ISqlDataSource.Builder> builderClass;

	SqlFactory(Class<? extends ISqlDataSource.Builder> builderClass) {
		this.builderClass = builderClass;
	}

	public ISqlDataSource create(IEvnConfig dbConfig, IEvnConfig propertyConfig){
		try {
			@SuppressWarnings("unchecked")
			ISqlDataSource.Builder builder = builderClass.newInstance();
			return builder.create(dbConfig, propertyConfig);
		}
		catch (IllegalAccessException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}
}
