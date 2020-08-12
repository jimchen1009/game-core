package com.game.common.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EvnConfigUtil {

	public static IEvnConfig loadConfig(String resourceName){
		File file = new File(resourceName);
		return loadConfig(file);
	}

	public static IEvnConfig loadConfig(File file){
		Config loadConfig = ConfigFactory.parseFile(file);
		return new EvnConfigs(loadConfig);
	}

	public static Map<String, IEvnConfig> loadConfigs(String resourceDirectory){
		File directory = new File(resourceDirectory);
		File[] files = directory.listFiles();
		if (files == null){
			return Collections.emptyMap();
		}
		Map<String, IEvnConfig> configMap = new HashMap<>(files.length);
		for (File file : files) {
			configMap.put(file.getName(), loadConfig(file));
		}
		return configMap;
	}
}
