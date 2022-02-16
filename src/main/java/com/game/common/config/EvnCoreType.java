package com.game.common.config;

public enum EvnCoreType {
	CACHE("core_cache_config.conf"),
	REDIS("core_redis_config.conf"),
	MONGO("core_mongo_config.conf"),
	MYSQL("core_mysql_config.conf"),
	;

	private final String defaultName;

	EvnCoreType(String defaultName) {
		this.defaultName = defaultName;
	}

	public String getName() {
		return defaultName;
	}
}
