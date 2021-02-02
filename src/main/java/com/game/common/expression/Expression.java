package com.game.common.expression;

import jodd.util.StringUtil;

import java.math.BigDecimal;

public abstract class Expression implements IExpression {

	private final String expression;

	public Expression(String expression) {
		this.expression = expression;
	}

	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public long calculateLong(IExprParams params) {
		if (StringUtil.isEmpty(expression)){
			return 0;
		}
		Number longValue = execute(params);
		return longValue.longValue();
	}

	@Override
	public long calculateLong() {
		return calculateLong(ExprParams.EMPTY);
	}

	@Override
	public int calculateInt(IExprParams params) {
		if (StringUtil.isEmpty(expression)){
			return 0;
		}
		Number intValue = execute(params);
		return intValue.intValue();
	}

	@Override
	public int calculateInt() {
		return calculateInt(ExprParams.EMPTY);
	}

	@Override
	public double calculateDouble(IExprParams params) {
		if (StringUtil.isEmpty(expression)){
			return 0;
		}
		Number doubleValue = execute(params);
		return doubleValue.doubleValue();
	}

	@Override
	public double calculateDouble() {
		return calculateDouble(ExprParams.EMPTY);
	}

	@Override
	public BigDecimal calculateDecimal(IExprParams params) {
		if (StringUtil.isEmpty(expression)){
			return new BigDecimal(0);
		}
		return execute(params);
	}

	@Override
	public BigDecimal calculateDecimal() {
		return calculateDecimal(ExprParams.EMPTY);
	}

	@Override
	public String calculateString(IExprParams params) {
		if (expression == null){
			return null;
		}
		if (expression.isEmpty()){
			return expression;
		}
		return execute(params);
	}

	@Override
	public String calculateString() {
		return calculateString(ExprParams.EMPTY);
	}

	protected abstract <T> T execute(IExprParams params);
}
