package com.game.core.db.sql;

import com.game.common.config.IEvnConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public class HikariCPDataSource implements ISqlDataSource {

	private final String dbName;
	private HikariDataSource dataSource;

	public HikariCPDataSource(String dbName, HikariDataSource dataSource) {
		this.dbName = dbName;
		this.dataSource = dataSource;
	}

	@Override
	public String dbName() {
		return dbName;
	}

	@Override
	public DataSource getImpl() {
		return dataSource;
	}

	@Override
	public void close() throws IOException {
		dataSource.close();
	}

	public static class Builder implements ISqlDataSource.Builder{

		@Override
		public ISqlDataSource create(IEvnConfig dbConfig, IEvnConfig propertyConfig) {
			String dbName = dbConfig.getString("db_name");
			String host = dbConfig.getString("host");
			int port = dbConfig.getInt("port");
			String username = dbConfig.getString("username");
			String password = dbConfig.getString("password");

			Properties properties = propertyConfig.toProperties();
			String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
			properties.put("jdbcUrl", jdbcUrl);
			properties.put("username", username);
			properties.put("password", password);

			HikariConfig hikariConfig = new HikariConfig(properties);
			return new HikariCPDataSource(dbName, new HikariDataSource(hikariConfig));
		}
	}
}
