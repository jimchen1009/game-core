package com.game.core.cache.data;

public class DataPrivilegeUtil {

	public static <K> boolean existIndexBit(IData<K> data, int index) {
		return ((Data<K>)data).existIndexBit(index);
	}

	public static <K> void clearIndexBit(IData<K> data, int index){
		((Data<K>)data).clearIndexBit(index);
	}

	public static <K> void setIndexBit(IData<K> data, int index) {
		((Data<K>)data).setIndexBit(index);
	}

//	public static <K> void setCacheBitValue(IData<K> data, int cacheBitIndex) {
//		((Data<K>)data).setCacheBitValue(cacheBitIndex);
//	}
//
//	public static <K> long getCacheBitValue(IData<K> data) {
//		return ((Data<K>)data).getCacheBitValue();
//	}
//
//	public static <K> void clearCacheBitValue(IData<K> data) {
//		((Data<K>)data).clearCacheBitValue();
//	}
}
