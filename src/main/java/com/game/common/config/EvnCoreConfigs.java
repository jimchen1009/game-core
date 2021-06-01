package com.game.common.config;

import jodd.util.StringUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
		String property = System.getProperty("game.core.config.path");
		String filename = StringUtil.isEmpty(property) ? coreType.getName() : (property + File.separatorChar + coreType.getName());
		return EvnConfigUtil.loadConfig(filename);
	}
}
