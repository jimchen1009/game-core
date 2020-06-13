package com.game.cache;

import com.game.cache.mapper.ClassConfig;
import com.game.cache.mapper.ClassInformation;
import jodd.util.StringUtil;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CacheDaoUnique implements ICacheDaoUnique {

	protected final ClassConfig classConfig;
	protected final List<Map.Entry<String, Object>> primaryUniqueKeys;
	private final ClassInformation information;
	protected final String daoUniqueId;
	protected final String formatRedisKey;

	public CacheDaoUnique(ClassConfig classConfig, List<Map.Entry<String, Object>> primaryUniqueKeys) {
		this.classConfig = classConfig;
		this.primaryUniqueKeys = primaryUniqueKeys;
		this.information = ClassInformation.get(classConfig.className);
		this.daoUniqueId = classConfig.getClass().getName() + "_" + classConfig.primarySharedId + "_" + StringUtil.join(information.getCombineUniqueKeys(),",");
		List<String> strings = new ArrayList<>(information.getPrimaryUniqueKeys());
		strings.remove(information.getPrimaryKey());
		String primarySharedId = "";
		if (classConfig.primarySharedId > 0){
			primarySharedId = "." + classConfig.primarySharedId;
		}
		if (strings.isEmpty()) {
			this.formatRedisKey = "100:%s_" + String.format("%s%s.v%s", classConfig.tableName , primarySharedId, classConfig.versionId);
		}
		else {
			String string = StringUtil.join(strings, ".");
			this.formatRedisKey = "100:%s." + String.format("%s_%s%s.v%s", string, classConfig.tableName , primarySharedId, classConfig.versionId);
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
	public String getDaoUniqueId() {
		return daoUniqueId;
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
	public String getTableName() {
		return classConfig.tableName;
	}

	@Override
	public int getPrimarySharedId() {
		return classConfig.primarySharedId;
	}


	public List<ICacheDaoUnique> sharedCacheDaoUniqueList(){
		List<ClassConfig> sharedConfigList = ClassConfig.getPrimarySharedConfigList(getTableName());
		List<ICacheDaoUnique> cacheDaoUniqueList = sharedConfigList.stream().filter(config -> config.primarySharedId != getPrimarySharedId())
				.map(config -> new CacheDaoUnique(config, primaryUniqueKeys)).collect(Collectors.toList());
		return cacheDaoUniqueList;
	}

	@Override
	public boolean isUserCache() {
		return classConfig.isUserClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ICacheDaoUnique that = (ICacheDaoUnique) o;
		return Objects.equals(getDaoUniqueId(), that.getDaoUniqueId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(daoUniqueId);
	}

	@Override
	public String toString() {
		return "{" +
				"daoUniqueId='" + daoUniqueId + '\'' +
				'}';
	}

	public static CacheDaoUnique create(Class<?> aClass) {
		return create(aClass, Collections.emptyList());
	}

	public static CacheDaoUnique create(Class<?> aClass, List<Map.Entry<String, Object>> appendKeyList) {
		ClassInformation information = ClassInformation.get(aClass);
		List<String> primaryUniqueKeys = information.getPrimaryUniqueKeys();
		int indexOf = primaryUniqueKeys.indexOf(information.getPrimaryKey());
		List<Map.Entry<String, Object>> primaryUniqueKeys0 = new ArrayList<>(appendKeyList);
		primaryUniqueKeys0.add(indexOf, new AbstractMap.SimpleEntry<>(information.getPrimaryKey(), null));
		return new CacheDaoUnique(ClassConfig.getConfig(aClass), primaryUniqueKeys0);
	}
}
