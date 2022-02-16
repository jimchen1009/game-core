package com.game.core.cache.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class Data<K> implements IData<K> {

    private static final Logger logger = LoggerFactory.getLogger(Data.class);

    private int $cacheBitValue$ = 0;

    boolean existIndexBit(int index) {
        return (this.$cacheBitValue$ & (1L << index)) != 0;
    }

    void clearIndexBit(int index){
        $cacheBitValue$ = this.$cacheBitValue$ ^ (1 << index);
    }

    void setIndexBit(int index) {
        this.$cacheBitValue$ = this.$cacheBitValue$ | (1 << index);
    }

//    void setCacheBitValue(int cacheBitValue) {
//        this.$cacheBitValue$ = cacheBitValue;
//    }
//
//    long getCacheBitValue() {
//        return $cacheBitValue$;
//    }
//
//    void clearCacheBitValue() {
//        this.$cacheBitValue$ = 0;
//    }

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
}
