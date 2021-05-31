package com.game.core.cache;

import com.game.core.cache.mapper.ClassAnnotation;
import com.game.core.cache.mapper.FieldAnnotation;
import com.game.core.cache.mapper.annotation.CacheIndexes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CacheUniqueId implements ICacheUniqueId {

	private static final Logger logger = LoggerFactory.getLogger(CacheUniqueId.class);

	protected final ClassConfig classConfig;
	protected final List<Map.Entry<String, Object>> primaryUniqueKeys;
	private final ClassAnnotation information;
	protected final String sourceUniqueId;
	protected final String redisPrimaryKeyFormatString;

	/***
	 * @param classConfig
	 */
	public CacheUniqueId(ClassConfig classConfig) {
		this.classConfig = classConfig;
		this.information = ClassAnnotation.create(classConfig.getAClass());
		this.primaryUniqueKeys = new ArrayList<>(1);
		primaryUniqueKeys.add(new AbstractMap.SimpleEntry<>(information.getPrimaryKey(), null));
		sourceUniqueId = String.format("%s_%s", classConfig.getName(), classConfig.getPrimarySharedId());;
		this.redisPrimaryKeyFormatString = createRedisPrimaryKeyFormatString();

	}

	@Override
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
	public <V> Class<V> getAClass() {
		return classConfig.getAClass();
	}

	@Override
	public CacheType getCacheType() {
		return classConfig.getCacheType();
	}

	@Override
	public String getName() {
		return classConfig.getName();
	}

	@Override
	public int getPrimarySharedId() {
		return classConfig.getPrimarySharedId();
	}

	@Override
	public boolean isDelayUpdate() {
		return classConfig.isDelayUpdate();
	}

	@Override
	public int getVersionId() {
		return classConfig.getVersionId();
	}

	@Override
	public IClassConfig cloneConfig() {
		return classConfig.cloneConfig();
	}

	@Override
	public boolean isAccountCache() {
		return classConfig.isAccountCache();
	}

	@Override
	public boolean isCacheLoadAdvance() {
		return classConfig.isCacheLoadAdvance();
	}

	@Override
	public boolean isRedisSupport() {
		return classConfig.isRedisSupport();
	}

	@Override
	public String getRedisKeyString(long primaryKey) {
		return String.format(redisPrimaryKeyFormatString, primaryKey);
	}

	@Override
	public CacheIndexes getCacheIndexes() {
		return information.getCacheIndexes();
	}

	@Override
	public String getPrimaryKey() {
		return information.getPrimaryKey();
	}

	@Override
	public List<String> getSecondaryKeyList() {
		return information.getSecondaryKeyList();
	}

	@Override
	public List<String> getCombineUniqueKeyList() {
		return information.getCombineUniqueKeyList();
	}

	@Override
	public List<FieldAnnotation> getFiledAnnotationList() {
		return information.getFiledAnnotationList();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CacheUniqueId that = (CacheUniqueId) o;
		return Objects.equals(sourceUniqueId, that.sourceUniqueId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sourceUniqueId);
	}

	private String createRedisPrimaryKeyFormatString(){
		String primarySharedId = "";
		if (classConfig.getPrimarySharedId() > 0){
			primarySharedId = "." + classConfig.getPrimarySharedId();
		}
		return "100:%s_" + String.format("%s%s.v%s", classConfig.getName() , primarySharedId, classConfig.getVersionId());
	}
}
