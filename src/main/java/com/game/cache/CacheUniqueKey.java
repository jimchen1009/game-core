package com.game.cache;

import com.game.cache.config.ClassConfig;
import com.game.cache.mapper.ClassInformation;
import jodd.util.StringUtil;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CacheUniqueKey implements ICacheUniqueKey {

	protected final ClassConfig classConfig;
	protected final List<Map.Entry<String, Object>> primaryUniqueKeys;
	private final ClassInformation information;
	protected final String stringUniqueId;
	protected final String formatRedisKey;

	public CacheUniqueKey(ClassConfig classConfig, List<Map.Entry<String, Object>> primaryUniqueKeys) {
		this.classConfig = classConfig;
		this.primaryUniqueKeys = primaryUniqueKeys;
		this.information = ClassInformation.get(classConfig.className);
		this.stringUniqueId = classConfig.getClass().getName() + "_" + classConfig.primarySharedId + "_" + StringUtil.join(information.getCombineUniqueKeys(),",");
		List<String> strings = new ArrayList<>(information.getPrimaryUniqueKeys());
		strings.remove(information.getPrimaryKey());
		String primarySharedId = "";
		if (classConfig.primarySharedId > 0){
			primarySharedId = "." + classConfig.primarySharedId;
		}
		if (strings.isEmpty()) {
			this.formatRedisKey = "100:%s_" + String.format("%s%s.v%s", classConfig.name , primarySharedId, classConfig.versionId);
		}
		else {
			String string = StringUtil.join(strings, ".");
			this.formatRedisKey = "100:%s." + String.format("%s_%s%s.v%s", string, classConfig.name , primarySharedId, classConfig.versionId);
		}
	}

	public ClassInformation getInformation() {
		return information;
	}

	public List<Map.Entry<String, Object>> createPrimaryUniqueKeys(long primaryKey) {
		List<Map.Entry<String, Object>> entryList = new ArrayList<>(primaryUniqueKeys.size());
		for (Map.Entry<String, Object> entry : primaryUniqueKeys) {
			if (entry.getValue() == null){
				entryList.add(new AbstractMap.SimpleEntry<>(entry.getKey(), primaryKey));
			}
			else {
				entryList.add(entry);
			}
		}
		return entryList;
	}

	@Override
	public String getStringUniqueId() {
		return stringUniqueId;
	}

	@Override
	public String getRedisKeyString(long primaryKey) {
		return String.format(formatRedisKey, primaryKey);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> Class<V> getAClass() {
		return (Class<V>)information.getAClass();
	}

	@Override
	public ClassConfig getClassConfig() {
		return classConfig;
	}

	@Override
	public String getName() {
		return classConfig.name;
	}

	@Override
	public int getPrimarySharedId() {
		return classConfig.primarySharedId;
	}


	public List<ICacheUniqueKey> sharedCacheDaoUniqueList(){
		List<ClassConfig> sharedConfigList = ClassConfig.getPrimarySharedConfigList(getName());
		List<ICacheUniqueKey> cacheDaoUniqueList = sharedConfigList.stream()
				.map(config -> new CacheUniqueKey(config, primaryUniqueKeys)).collect(Collectors.toList());
		return cacheDaoUniqueList;
	}

	@Override
	public boolean isAccountCache() {
		return classConfig.accountCache;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ICacheUniqueKey that = (ICacheUniqueKey) o;
		return Objects.equals(getStringUniqueId(), that.getStringUniqueId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(stringUniqueId);
	}


	@Override
	public String toString() {
		return "{" +
				"classConfig=" + classConfig +
				", primaryUniqueKeys=" + primaryUniqueKeys +
				", information=" + information +
				", stringUniqueId='" + stringUniqueId + '\'' +
				", formatRedisKey='" + formatRedisKey + '\'' +
				'}';
	}

	public static CacheUniqueKey create(Class<?> aClass) {
		return create(aClass, Collections.emptyList());
	}

	public static CacheUniqueKey create(Class<?> aClass, List<Map.Entry<String, Object>> appendKeyList) {
		ClassInformation information = ClassInformation.get(aClass);
		List<String> primaryUniqueKeys = information.getPrimaryUniqueKeys();
		int indexOf = primaryUniqueKeys.indexOf(information.getPrimaryKey());
		List<Map.Entry<String, Object>> primaryUniqueKeys0 = new ArrayList<>(appendKeyList);
		primaryUniqueKeys0.add(indexOf, new AbstractMap.SimpleEntry<>(information.getPrimaryKey(), null));
		return new CacheUniqueKey(ClassConfig.getConfig(aClass), primaryUniqueKeys0);
	}
}
