package com.game.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtil {

	public static final ZoneId ZONE_ID = ZoneId.systemDefault();

	public static LocalDateTime nowLocalDateTime(){
		return LocalDateTime.now(ZONE_ID);
	}
}
