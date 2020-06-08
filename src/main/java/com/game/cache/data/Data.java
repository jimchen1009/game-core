package com.game.cache.data;

import com.game.cache.CacheUniqueId;
import com.game.cache.mapper.annotation.CacheFiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class Data<K> implements IData<K> {

    private static final Logger logger = LoggerFactory.getLogger(Data.class);
    private static final long VALUE_ERASER;
    static {
        long value = 0;
        for (int uniqueId = 0; uniqueId <= CacheUniqueId.MAX_ID; uniqueId++) {
            value = value | (1L << uniqueId);
        }
        VALUE_ERASER = ~value;
    }

    @CacheFiled(index = 63, name = "f1", isInternal = true)
    private long dataBitIndexBits = 0;

    @Override
    public boolean hasBitIndex(DataBitIndex bitIndex) {
        return (this.dataBitIndexBits & (1L << bitIndex.getUniqueId())) != 0;
    }

    private void clearBitIndex(DataBitIndex bitIndex){
        dataBitIndexBits = this.dataBitIndexBits ^ (1L << bitIndex.getUniqueId());
    }

    private void setBitIndex(DataBitIndex bitIndex) {
        this.dataBitIndexBits = this.dataBitIndexBits | (1L << bitIndex.getUniqueId());
    }

    @Override
    public long getBitIndexBits() {
        return dataBitIndexBits;
    }

    @Override
    public void clearCacheBitIndex() {
        this.dataBitIndexBits = this.dataBitIndexBits & VALUE_ERASER;
    }

    public void onIndexValueChanged(int uniqueId){
        if (uniqueId > CacheUniqueId.MAX_ID){
            throw new UnsupportedOperationException(String.valueOf(uniqueId));
        }
        dataBitIndexBits = dataBitIndexBits | (1 << uniqueId);
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
