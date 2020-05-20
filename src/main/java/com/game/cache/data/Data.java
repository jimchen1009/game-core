package com.game.cache.data;

import com.game.cache.mapper.annotation.ResourceField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class Data<K> implements IData<K> {

    private static final Logger logger = LoggerFactory.getLogger(Data.class);

    private transient long indexChangedBits = 0L;        //目前只支持64个字段的类~

    @ResourceField
    private boolean isCacheResource = false;             //数据是否从缓存加载的~

    public final boolean isCacheResource() {
        return isCacheResource;
    }

    public boolean isIndexChanged(int uniqueId){
        return (indexChangedBits & (1L << uniqueId)) != 0;
    }

    public long getIndexChangedBits() {
        return indexChangedBits;
    }

    public void clearIndexChangedBits(){
        indexChangedBits = 0L;
    }

    public void onIndexValueChanged(int index){
        indexChangedBits = indexChangedBits | (1L << index);
    }

    public final Object clone(Supplier<Object> supplier) {
        try {
            return super.clone();
        }
        catch (Throwable e) {
            logger.error("{}", this.getClass().getName(), e);
        }
        return supplier.get();
    }
}
