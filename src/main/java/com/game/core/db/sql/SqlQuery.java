package com.game.core.db.sql;

import java.util.Arrays;
import java.util.List;

public class SqlQuery<T> {

	private final String cmd;
	private final Object[] params;
	private final SqlObjectBuilder<T> builder;

	public SqlQuery(String cmd, Object[] params, SqlObjectBuilder<T> builder) {
		this.cmd = cmd;
		this.params = params;
		this.builder = builder;
	}

	public String getCmd() {
		return cmd;
	}

	public Object[] getParams() {
		return params;
	}

	public SqlObjectBuilder<T> getBuilder() {
		return builder;
	}


	public String exceptionString() {
		return "{" +
				"cmd='" + cmd + '\'' +
				", params=" + Arrays.toString(params) +
				'}';
	}

	public static class Result<T>{

		private final SqlQuery<T> query;
		private final List<T> objectList;

		public Result(SqlQuery<T> query, List<T> objectList) {
			this.query = query;
			this.objectList = objectList;
		}

		public SqlQuery<T> getQuery() {
			return query;
		}

		public List<T> getObjectList() {
			return objectList;
		}

		public T firstOne(){
			return objectList.isEmpty() ?  null :  objectList.get(0);
		}
	}
}
