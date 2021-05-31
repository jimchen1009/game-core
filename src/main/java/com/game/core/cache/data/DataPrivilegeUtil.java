package com.game.core.cache.data;

import com.game.common.log.LogUtil;
import com.game.core.cache.exception.CacheException;

import java.lang.reflect.Method;

public class DataPrivilegeUtil {

	private static final Method setBitIndex = lookupClassMethod("setBitIndex");
	private static final Method clearBitIndex = lookupClassMethod("clearBitIndex");
	private static final Method setBitValue = lookupClassMethod("setBitValue");

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

	public static void invokeSetBitIndex(IData dataValue, int index) {
		dataValueInvoke(dataValue, setBitIndex, index);
	}

	public static void invokeClearBitIndex(IData dataValue, int index) {
		dataValueInvoke(dataValue, clearBitIndex, index);
	}


	public static void invokeSetBitValue(IData dataValue, long bitValue) {
		dataValueInvoke(dataValue, setBitValue, bitValue);
	}

	private static void dataValueInvoke(IData dataValue, Method method, Object... args) {
		try {
			method.invoke(dataValue, method);
		}
		catch (Throwable e) {
			throw new CacheException("%s, %s", e, LogUtil.toJSONString(args), LogUtil.toJSONString(dataValue));
		}
	}

}
