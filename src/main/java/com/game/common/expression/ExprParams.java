package com.game.common.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExprParams implements IExprParams{

	public static final ExprParams EMPTY = new ExprParams(Collections.emptyMap());

	private final Map<String, Object> map;

	public ExprParams(Map<String, Object> map) {
		this.map = map;
	}

	public ExprParams() {
		this(new HashMap<>());
	}

	@Override
	public void put(String key, Object value) {
		map.put(key, value);
	}

	@Override
	public Map<String, Object> getMap() {
		return map;
	}
}
