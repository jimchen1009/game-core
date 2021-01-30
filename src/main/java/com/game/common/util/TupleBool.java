package com.game.common.util;

public class TupleBool<T> {

	private final boolean aBoolean;
	private final T data;

	public TupleBool(boolean aBoolean, T data) {
		this.aBoolean = aBoolean;
		this.data = data;
	}

	public TupleBool(boolean aBoolean) {
		this(aBoolean, null);
	}

	public TupleBool(T data) {
		this(true, data);
	}

	public boolean isSuccess() {
		return aBoolean;
	}

	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		return "{" +
				"aBoolean=" + aBoolean +
				", data=" + data +
				'}';
	}
}
