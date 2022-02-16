package com.game.core.db.sql;

import com.game.common.config.IEvnConfig;

import javax.sql.DataSource;
import java.io.Closeable;

public interface ISqlDataSource extends Closeable {

	String dbName();

	DataSource getImpl();

	interface Builder{
		ISqlDataSource create(IEvnConfig dbConfig, IEvnConfig globalConfig);
	}
}
