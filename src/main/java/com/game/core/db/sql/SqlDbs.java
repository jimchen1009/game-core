package com.game.core.db.sql;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;
import com.game.common.config.IEvnConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlDbs {

	private static final Logger logger = LoggerFactory.getLogger(SqlDb.class);

	private static final Map<String, SqlDb> DB_MAP = new HashMap<>();

	public static void initialize(){
		IEvnConfig evnConfig = EvnCoreConfigs.getInstance(EvnCoreType.MYSQL).getConfig("mysql");
		IEvnConfig globalConfig = evnConfig.getConfig("global");
		List<IEvnConfig> configList = evnConfig.getConfigList("dbs");
		for (IEvnConfig iEvnConfig : configList) {
			ISqlDataSource dataSource = create(iEvnConfig, globalConfig);
			SqlDb SQLDb = new SqlDb(dataSource);
			DB_MAP.put(SQLDb.dbName(), SQLDb);
		}
	}

	public static void destroyAll(){
		DB_MAP.values().forEach(SqlDb::close);
	}

	public static SqlDb get(String name){
		return DB_MAP.get(name);
	}

	private static ISqlDataSource create(IEvnConfig dbConfig, IEvnConfig globalConfig){
		SqlFactory factory = SqlFactory.valueOf(globalConfig.getString("factory"));
		IEvnConfig propertyConfig = globalConfig.getConfig("properties");
		return factory.create(dbConfig, propertyConfig);
	}

}
