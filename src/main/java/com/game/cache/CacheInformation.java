package com.game.cache;

import java.util.EnumMap;

/**
 * 缓存的信息的额外描述信息~
 */
public class CacheInformation {

    private final EnumMap<InformationName, Object> name2Values;

    public CacheInformation() {
        this.name2Values = new EnumMap<>(InformationName.class);
    }

    private CacheInformation(EnumMap<InformationName, Object> name2Values) {
        this.name2Values = new EnumMap<>(name2Values);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(InformationName informationName){
        return (T)name2Values.get(informationName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(InformationName informationName, T defaultValue){
        return (T)name2Values.getOrDefault(informationName, defaultValue);
    }

    public CacheInformation copy(){
        return new CacheInformation(this.name2Values);
    }

    public boolean isEmpty(){
        return name2Values.isEmpty();
    }
}
