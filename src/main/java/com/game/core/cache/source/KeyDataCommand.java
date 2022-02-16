package com.game.core.cache.source;

import com.game.core.cache.data.IData;

public class KeyDataCommand<K, V extends IData<K>> {

    private final K key;
    private final CacheCommand command;
    private final V data;
    private final long versionId;

    public KeyDataCommand(K key, CacheCommand command, V data, long versionId) {
        this.key = key;
        this.command = command;
        this.data = data;
        this.versionId = versionId;
    }

    public K getKey() {
        return key;
    }

    public V getData() {
        return data;
    }

    public CacheCommand getCommand() {
        return command;
    }

    public long getVersionId() {
        return versionId;
    }

    public boolean isUpsert(){
        return command.equals(CacheCommand.UPSERT);
    }

    public boolean isDeleted(){
        return command.equals(CacheCommand.DELETE);
    }

    public static <K, V extends IData<K>> KeyDataCommand<K, V> upsertCommand(V dataValue, long versionId){
        return new KeyDataCommand<>(dataValue.secondaryKey(), CacheCommand.UPSERT, dataValue, versionId);
    }

    public static <K, V extends IData<K>> KeyDataCommand<K, V> deleteCommand(K key, long versionId){
        return new KeyDataCommand<>(key, CacheCommand.DELETE, null, versionId);
    }
}
