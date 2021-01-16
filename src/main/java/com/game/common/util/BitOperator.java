package com.game.common.util;

import java.util.ArrayList;
import java.util.List;

public class BitOperator {

	private final static int MAX_BITS = 30;

	private static int[] MASKS_ON;

	static {
		MASKS_ON = new int[MAX_BITS];
		for (int i = 0; i < MAX_BITS; i++) {
			MASKS_ON[i] = (int) Math.pow(2, i);
		}
	}

	/**
	 * 检测某一位的值是否为1
	 */
	public static boolean checkBitValue(int value, int bitIndex) {
		return (value & MASKS_ON[bitIndex]) > 0;
	}

	/**
	 * 设置某一位的值为1
	 *
	 * @return 操作后的新值
	 */
	public static int setBitValue(int oldValue, int bitIndex) {
		return oldValue | MASKS_ON[bitIndex];
	}

	/**
	 * 设置某一位的值为0
	 *
	 * @return 操作后的新值
	 */
	public static int clearBitValue(int oldValue, int bitIndex) {
		if (checkBitValue(oldValue, bitIndex)) {
			return oldValue ^ MASKS_ON[bitIndex];
		} else {
			return oldValue;
		}
	}

	/**
	 * 计数二进制中1的个数
	 * @param value
	 * @return 二进制形式1的个数
	 */
	public static int getBitCount(int value){
		int n = 0;
		for(; value > 0; ++n){
			value &= (value - 1);
		}
		return n;
	}

	/**
	 * 获取二进制中1的位置
	 * @param value
	 * @return
	 */
	public static List<Integer> getBitIndex(int value){
		List<Integer> indexList = new ArrayList<>();
		int index = 0;
		while (value > 0){
			if (checkBitValue(value, 0)) {
				indexList.add(index);
			}
			value = value>>1;
			index++;
		}
		return indexList;
	}
}
