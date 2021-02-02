package com.game.common.expression;

import com.game.common.expression.aviator.AviatorExpression;

public class ExprManager {

	private static ExprManager instance = new ExprManager();

	public static ExprManager getInstance() {
		return instance;
	}

	public IExpression createExpression(String expression){
		return new AviatorExpression(expression);
	}
}
