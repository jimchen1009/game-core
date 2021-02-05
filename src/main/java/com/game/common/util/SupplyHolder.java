package com.game.common.util;

import java.util.function.Supplier;

public class SupplyHolder<T> {

    private T value;
    private final Supplier<T> supplier;

    public SupplyHolder(Supplier<T> supplier) {
        this.supplier = supplier;
        this.value = null;
    }

    public T getValueOnly() {
        return value;
    }

    public T computeIfAbsent() {
        if (value == null){
            value = supplier.get();
        }
        return value;
    }
}
