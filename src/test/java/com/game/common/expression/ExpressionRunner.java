package com.game.common.expression;

import org.junit.Test;

import java.util.Collections;

public class ExpressionRunner {


	@Test
	public void run(){
		IExpression expression = ExprManager.getInstance().createExpression("2*100+10");
		System.out.println(expression.getExpression() + " = " + expression.calculateInt());

		expression = ExprManager.getInstance().createExpression("2*a+10");
		ExprParams exprParams = new ExprParams(Collections.singletonMap("a", 100));
		System.out.println(expression.getExpression() + " = " + expression.calculateInt(exprParams));
	}
}
