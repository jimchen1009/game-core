package com.game.core.db.sql;

import com.game.core.db.DbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SqlDbExecutor {

	private static final Logger logger = LoggerFactory.getLogger(SqlDbExecutor.class);

	private final ISqlDataSource dataSource;

	public SqlDbExecutor(ISqlDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String dbName() {
		return dataSource.dbName();
	}

	public void close() {
		try {
			dataSource.close();
		}
		catch (IOException e) {
			logger.error("{}", dbName(), e);
		}
	}

	public int executeCommand(String cmd, Object[] params) {
		return getConnection(connection -> {
			PreparedStatement stmt = null;
			int rowCounts;
			try {
				stmt = connection.prepareStatement(cmd);
				SqlDbUtil.setParams(stmt, params);
				rowCounts = stmt.executeUpdate();
			}
			catch (SQLException e) {
				throw new DbException(cmd, e);
			}
			finally {
				SqlDbUtil.releaseStatement(stmt);
			}
			return rowCounts;
		});
	}

	public int[] executeBatchCommand(String cmd, Collection<Object[]> paramsCollection) {
		return getConnection(connection -> {
			PreparedStatement stmt = null;
			int[] rowCounts = null;
			try {
				stmt = connection.prepareStatement(cmd);
				SqlDbUtil.setBatchParams(stmt, paramsCollection);
				rowCounts = stmt.executeBatch();
			}
			catch (SQLException e) {
				throw new DbException(cmd, e);
			}
			finally {
				SqlDbUtil.releaseStatement(stmt);
			}
			return rowCounts;
		});
	}


	public <T> SqlQuery.Result<T> executeQueryObjectList(SqlQuery<T> query){
		return getConnection(connection -> executeQueryObjectList(connection, query));
	}

	@SuppressWarnings("unchecked")
	public SqlQueryBatch.Result executeQueryObjectList(SqlQueryBatch queryBatch){
		return getConnection(connection -> {
			List<SqlQuery> sqlQueryList = queryBatch.getQueryList();
			List<SqlQuery.Result> resultList = new ArrayList<>(sqlQueryList.size());
			for (SqlQuery sqlQuery : sqlQueryList) {
				SqlQuery.Result result = executeQueryObjectList(sqlQuery);
				resultList.add(result);
			}
			return new SqlQueryBatch.Result(resultList);
		});
	}

	private  <T> T getConnection(Function<Connection, T> function){
		Connection connection = null;
		try {
			connection = dataSource.getImpl().getConnection();
			return function.apply(connection);
		}
		catch (SQLException e){
			throw new DbException("getConnection", e);
		}
		finally {
			SqlDbUtil.releaseConnection(connection);
		}
	}

	private <T> SqlQuery.Result<T> executeQueryObjectList(Connection connection, SqlQuery<T> query) {
		List<T> objectList = new ArrayList<>(10);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(query.getCmd());
			SqlDbUtil.setParams(stmt, query.getParams());
			rs = stmt.executeQuery();
			SqlDbUtil.buildObjectToList(rs, objectList, query.getBuilder());
			return new SqlQuery.Result<>(query, objectList);
		}
		catch (SQLException e) {
			throw new DbException(query.exceptionString(), e);
		}
		finally {
			SqlDbUtil.releaseResultSet(rs);
			SqlDbUtil.releaseStatement(stmt);
		}
	}
}
