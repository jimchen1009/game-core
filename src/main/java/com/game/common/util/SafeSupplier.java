package com.game.common.util;

import java.util.function.Supplier;

public class SafeSupplier<T> implements Supplier<T>{

	private final Supplier<T> supplier;
	private final T failureValue;

	public SafeSupplier(Supplier<T> supplier, T failureValue) {
		this.supplier = supplier;
		this.failureValue = failureValue;
	}

	@Override
	public T get() {
		try {
			return supplier.get();
		}
		catch (Throwable throwable){
			return failureValue;
		}
	}
}
