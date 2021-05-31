package com.game.common.config;

public enum EvnCoreType {
	CACHE("core_cache_config.conf"),
	DB("core_db_config.conf"),
	;

	private final String defaultName;

	EvnCoreType(String defaultName) {
		this.defaultName = defaultName;
	}

	public String getName() {
		return defaultName;
	}
}
