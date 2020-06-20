package com.game.common.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {

	public static IConfig loadConfig(String resourceName){
		File file = new File(resourceName);
		return loadConfig(file);
	}

	public static IConfig loadConfig(File file){
		Config loadConfig = ConfigFactory.parseFile(file);
		return new Configs(loadConfig);
	}

	public static Map<String, IConfig> loadConfigs(String resourceDirectory){
		File directory = new File(resourceDirectory);
		File[] files = directory.listFiles();
		if (files == null){
			return Collections.emptyMap();
		}
		Map<String, IConfig> configMap = new HashMap<>(files.length);
		for (File file : files) {
			configMap.put(file.getName(), loadConfig(file));
		}
		return configMap;
	}
}
