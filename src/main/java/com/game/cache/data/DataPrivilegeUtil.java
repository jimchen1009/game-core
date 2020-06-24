package com.game.cache.data;

import com.game.cache.exception.CacheException;
import com.game.common.log.LogUtil;

import java.lang.reflect.Method;

public class DataPrivilegeUtil {

	private static final Method setBitIndex = lookupClassMethod("setBitIndex");
	private static final Method clearBitIndex = lookupClassMethod("clearBitIndex");
	private static final Method setDataBitIndexBits = lookupClassMethod("setDataBitIndexBits");

	private static Method lookupClassMethod(String name) {
		Method[] methods = Data.class.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equals(name)){
				method.setAccessible(true);
				return method;
			}
		}
		throw new CacheException("not found method:%s", name);
	}

	public static void invokeSetBitIndex(IData dataValue, DataBitIndex bitIndex) {
		try {
			setBitIndex.invoke(dataValue, bitIndex);
		}
		catch (Throwable e) {
			throw new CacheException("bitIndex:%s, %s", e, bitIndex.getId(), LogUtil.toJSONString(dataValue));
		}
	}

	public static void invokeClearBitIndex(IData dataValue, DataBitIndex bitIndex) {
		try {
			clearBitIndex.invoke(dataValue, bitIndex);
		}
		catch (Throwable e) {
			throw new CacheException("bitIndex:%s, %s", e, bitIndex.getId(), LogUtil.toJSONString(dataValue));
		}
	}


	public static void invokeSetDataBitIndexBits(IData dataValue, long dataBitIndexBits) {
		try {
			setDataBitIndexBits.invoke(dataValue, dataBitIndexBits);
		}
		catch (Throwable e) {
			throw new CacheException("dataBitIndexBits:%s, %s", e, dataBitIndexBits, LogUtil.toJSONString(dataValue));
		}
	}
}
