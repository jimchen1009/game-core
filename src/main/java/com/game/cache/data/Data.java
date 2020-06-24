package com.game.cache.data;

import com.game.cache.exception.CacheException;
import com.game.cache.mapper.annotation.CacheFiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class Data<K> implements IData<K> {

    private static final Logger logger = LoggerFactory.getLogger(Data.class);
    private static final long VALUE_ERASER;
    static {
        long value = 0;
        for (int uniqueId = 0; uniqueId <= DataBitIndex.MaximumIndex; uniqueId++) {
            value = value | (1L << uniqueId);
        }
        VALUE_ERASER = ~value;
    }

    @CacheFiled(index = DataBitIndex.MaximumIndex)
    private int deleteTime = 0;

    private long dataBitIndexBits = 0;

    @Override
    public boolean hasBitIndex(DataBitIndex bitIndex) {
        return (this.dataBitIndexBits & (1L << bitIndex.getId())) != 0;
    }

    private void clearBitIndex(DataBitIndex bitIndex){
        dataBitIndexBits = this.dataBitIndexBits ^ (1L << bitIndex.getId());
    }

    private void setBitIndex(DataBitIndex bitIndex) {
        this.dataBitIndexBits = this.dataBitIndexBits | (1L << bitIndex.getId());
    }

    private void setDataBitIndexBits(long dataBitIndexBits) {
        this.dataBitIndexBits = dataBitIndexBits;
    }

    @Override
    public long getBitIndexBits() {
        return dataBitIndexBits;
    }

    @Override
    public boolean existCacheBitIndex() {
        return (dataBitIndexBits & VALUE_ERASER) > 0;
    }

    @Override
    public void clearCacheBitIndex() {
        this.dataBitIndexBits = this.dataBitIndexBits & VALUE_ERASER;
    }

    public void onIndexValueChanged(int uniqueId){
        if (uniqueId > DataBitIndex.MaximumIndex){
            throw new UnsupportedOperationException(String.valueOf(uniqueId));
        }
        dataBitIndexBits = dataBitIndexBits | (1 << uniqueId);
    }

    @Override
    public final Object clone(Supplier<Object> supplier) {
        try {
            return super.clone();
        }
        catch (Throwable e) {
            logger.error("{}", this.getClass().getName(), e);
        }
        return supplier.get();
    }

    @Override
    public boolean isDeleted() {
        return deleteTime > 0;
    }

    @Override
    public void delete(long currentTime) {
        if (currentTime <= 0){
            throw new CacheException("currentTime==%s", currentTime);
        }
        deleteTime = (int)(currentTime / 1000);
    }
}
