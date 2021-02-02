package com.game.common.expression.aviator;

import com.game.common.expression.Expression;
import com.game.common.expression.IExprParams;
import com.googlecode.aviator.AviatorEvaluator;

public class AviatorExpression extends Expression {

	private final boolean compile;

	public AviatorExpression(String expression, boolean compile) {
		super(expression);
		this.compile = compile;
	}

	public AviatorExpression(String expression) {
		this(expression, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T execute(IExprParams params) {
		String expression = getExpression();
		if (compile) {
			return (T)AviatorEvaluator.compile(expression).execute(params.getMap());
		}
		else {
			return (T)AviatorEvaluator.execute(expression, params.getMap());
		}
	}
}
