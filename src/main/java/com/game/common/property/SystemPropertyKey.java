package com.game.common.property;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SystemPropertyKey {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private static final SystemProperties SystemProperties = new SystemProperties();

    public final int id;
    public final String property;
    public final String defaultValue;

    public SystemPropertyKey(String property, String defaultValue) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.property = property;
        this.defaultValue = defaultValue;
    }

    public final String getString(){
        return getValue(string -> string);
    }

    public final Boolean getBoolean(){
        return getValue(Boolean::parseBoolean);
    }

    public final Integer getInteger(){
        return getValue(Integer::parseInt);
    }

    public final Long getLong(){
        return getValue(Long::parseLong);
    }

    public <T> T getValue(Function<String, T> function){
        return SystemProperties.getValue(this, defaultValue, function);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemPropertyKey that = (SystemPropertyKey) o;
        return id == that.id &&
                Objects.equals(property, that.property);
    }


    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", property='" + property + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
