package com.game.common.expression;

import java.math.BigDecimal;

public interface IExpression {

	String getExpression();

	long calculateLong(IExprParams params);

	long calculateLong();

	int calculateInt(IExprParams params);

	int calculateInt();

	double calculateDouble(IExprParams params);

	double calculateDouble();

	BigDecimal calculateDecimal(IExprParams params);

	BigDecimal calculateDecimal();

	String calculateString(IExprParams params);

	String calculateString();
}
