package com.game.core.db.sql;

import java.util.List;

public class SqlQueryBatch {

	private List<SqlQuery> queryList;

	public SqlQueryBatch(List<SqlQuery> queryList) {
		this.queryList = queryList;
	}

	public List<SqlQuery> getQueryList() {
		return queryList;
	}


	public static class Result{

		private final List<SqlQuery.Result> resultList;

		public Result(List<SqlQuery.Result> resultList) {
			this.resultList = resultList;
		}

		public List<SqlQuery.Result> getResultList() {
			return resultList;
		}
	}
}
