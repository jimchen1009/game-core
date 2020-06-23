package com.game.cache.data;

import com.game.cache.exception.CacheException;
import com.game.common.log.LogUtil;

import java.lang.reflect.Method;

public class DataPrivilegeUtil {

	private static final Method bitIndexSetter = lookupClassMethod("setBitIndex");
	private static final Method bitIndexCleaner = lookupClassMethod("clearBitIndex");

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
			bitIndexSetter.invoke(dataValue, bitIndex);
		}
		catch (Throwable e) {
			throw new CacheException("bitIndex:%s, %s", e, bitIndex.getId(), LogUtil.toJSONString(dataValue));
		}
	}

	public static void invokeClearBitIndex(IData dataValue, DataBitIndex bitIndex) {
		try {
			bitIndexCleaner.invoke(dataValue, bitIndex);
		}
		catch (Throwable e) {
			throw new CacheException("bitIndex:%s, %s", e, bitIndex.getId(), LogUtil.toJSONString(dataValue));
		}
	}
}
