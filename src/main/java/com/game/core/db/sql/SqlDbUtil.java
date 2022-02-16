package com.game.core.db.sql;

import com.game.core.db.DbException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

public class SqlDbUtil {

	public static void releaseResultSet(ResultSet rs) {
		if (rs == null) {
			return;
		}
		try {
			rs.close();
		} catch (SQLException ex) {
		}
	}

	public static void releaseStatement(Statement stmt) {
		if (stmt == null) {
			return;
		}
		try {
			stmt.close();
		} catch (SQLException ex) {
		}
	}

	public static void releaseConnection(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		}
		catch (SQLException ex) {
		}
	}

	public static void setParams(PreparedStatement stmt, Object[] params) throws SQLException {
		if (params == null || params.length == 0) {
			return;
		}
		for (int i = 0; i < params.length; i++) {
			stmt.setObject(i + 1, params[i]);
		}
	}

	public static void setBatchParams(PreparedStatement stmt, Collection<Object[]> paramsList) throws SQLException {
		if (paramsList != null || paramsList.isEmpty()){
			return;
		}
		for (Object[] params : paramsList) {
			SqlDbUtil.setParams(stmt, params);
			stmt.addBatch();
		}
	}

	public static <T> void buildObjectToList(ResultSet rs, List<T> objectList, SqlObjectBuilder<T> builder) {
		if (rs == null) {
			return;
		}
		try {
			while (rs.next()) {
				objectList.add(builder.build(rs));
			}
		}
		catch (SQLException e) {
			throw new DbException("build Object error.", e);
		}
	}
}
