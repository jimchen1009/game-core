package com.game.common.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

	public static int nextInt(int bound) {
		return ThreadLocalRandom.current().nextInt(bound);
	}

	public static <T> T select(List<T> randomTList) {
		if (randomTList == null || randomTList.isEmpty()) {
			return null;
		}
		int index = nextInt(randomTList.size());
		return randomTList.get(index);
	}


	/**
	 * @param begin 包括
	 * @param end 不包括
	 * @return
	 */
	public static long nextLong(long begin, long end) {
		if (begin >= end) {
			return begin;
		}
		else {
			return ThreadLocalRandom.current().nextLong(end - begin) + begin;
		}
	}
}
