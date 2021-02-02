package com.game.common.expression;

import java.util.Map;

public interface IExprParams {

	void put(String key, Object value);

	Map<String, Object> getMap();
}
