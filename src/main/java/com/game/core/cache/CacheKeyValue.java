package com.game.core.cache;

import java.util.Objects;

public class CacheKeyValue{

	private final String key;
	private final Object value;

	public CacheKeyValue(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CacheKeyValue that = (CacheKeyValue) o;
		return key.equals(that.key) && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
}
