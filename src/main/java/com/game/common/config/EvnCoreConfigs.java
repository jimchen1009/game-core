package com.game.common.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EvnCoreConfigs {

	private static IEvnConfig coreConfig = EvnConfigUtil.loadConfig(System.getProperty("game.core.config.path", "core_config.conf"));

	public static boolean hasPath(String path) {
		return coreConfig.hasPath(path);
	}

	public static IEvnConfig getConfig(String path){
		return coreConfig.getConfig(path);
	}

	public static int getInt(String path){
		return coreConfig.getInt(path);
	}

	public static long getLong(String path){
		return coreConfig.getLong(path);
	}

	public static String getString(String path) {
		return coreConfig.getString(path);
	}

	public static boolean getBoolean(String path) {
		return coreConfig.getBoolean(path);
	}

	public static List<IEvnConfig> getConfigList(String path){
		return coreConfig.getConfigList(path);
	}

	public static <T> List<T> getList(String path){
		return coreConfig.getList(path);
	}

	public static long getDuration(String path, TimeUnit timeUnit) {
		return coreConfig.getDuration(path, timeUnit);
	}
}
