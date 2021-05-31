package com.game.common.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EvnCoreConfigs {

	private static Map<EvnCoreType, IEvnConfig> evnConfigMap = new ConcurrentHashMap<>();

	public static void initiliaze(){
		Arrays.stream(EvnCoreType.values()).forEach(EvnCoreConfigs::getCoreConfig);
	}

	public static IEvnConfig getInstance(EvnCoreType coreType){
		return getCoreConfig(coreType);
	}

	public static void reloadCoreConfig(EvnCoreType coreType){
		IEvnConfig coreConfig = createCoreConfig(coreType);
		evnConfigMap.put(coreType, coreConfig);
	}

	private static IEvnConfig getCoreConfig(EvnCoreType coreType){
		return evnConfigMap.computeIfAbsent(coreType, EvnCoreConfigs::createCoreConfig);
	}

	private static IEvnConfig createCoreConfig(EvnCoreType coreType){
		String filename = Objects.requireNonNull(System.getProperty("game.core.config.path")) + File.pathSeparator + coreType.getName();
		return EvnConfigUtil.loadConfig(filename);
	}

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
