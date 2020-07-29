package com.game.core.cache;

import com.game.core.cache.mapper.ClassAnnotation;
import com.game.core.cache.mapper.FieldAnnotation;
import com.game.core.cache.mapper.annotation.CacheIndexes;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CacheUniqueId implements ICacheUniqueId {

	private static final Logger logger = LoggerFactory.getLogger(CacheUniqueId.class);

	private static final Map<String, List<CacheUniqueId>> name2UniqueKeyList = new ConcurrentHashMap<>();

	private static final Set<CacheUniqueId> cacheUniqueIdSet = new HashSet<>();


	protected final ClassConfig classConfig;
	protected final String primaryFixKeyParams;
	protected final List<Map.Entry<String, Object>> primaryUniqueKeys;
	private final ClassAnnotation information;
	protected final String sourceUniqueId;
	protected final String redisKeyFormatString;

	/***
	 * @param classConfig
	 * @param primaryFixKeyParams key:value#key:value
	 */
	public CacheUniqueId(ClassConfig classConfig, String primaryFixKeyParams) {
		this.classConfig = classConfig;
		this.information = ClassAnnotation.create(classConfig.getAClass());
		this.primaryFixKeyParams = primaryFixKeyParams;
		String formatUniqueId = String.format("%s_%s_%s", classConfig.getName(), classConfig.getPrimarySharedId(), StringUtil.join(information.getCombineUniqueKeyList(), ","));
		if (StringUtil.isEmpty(primaryFixKeyParams)){
			this.primaryUniqueKeys = new ArrayList<>(1);
			primaryUniqueKeys.add(new AbstractMap.SimpleEntry<>(information.getPrimaryKey(), null));
			sourceUniqueId = formatUniqueId;
		}
		else {
			String[] stringList = primaryFixKeyParams.split("#");
			this.primaryUniqueKeys = new ArrayList<>(stringList.length);
			for (String string : stringList) {
				String[] strings = string.split("#");
				this.primaryUniqueKeys.add(new AbstractMap.SimpleEntry<>(strings[0], strings[1]));
			}
			int indexOf = information.getPrimaryKeyList().indexOf(information.getPrimaryKey());
			primaryUniqueKeys.add(new AbstractMap.SimpleEntry<>(information.getPrimaryKey(), null));
			sourceUniqueId = formatUniqueId + "_" + primaryFixKeyParams;
		}
		this.redisKeyFormatString = createRedisKeyFormatString();

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
	public String getSourceUniqueId() {
		return sourceUniqueId;
	}

	@Override
	public String getRedisKeyString(long primaryKey) {
		return String.format(redisKeyFormatString, primaryKey);
	}

	@Override
	public List<ICacheUniqueId> sharedCacheDaoUniqueList(){
		return Collections.singletonList(this);
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
	public List<String> getPrimaryKeyList() {
		return information.getPrimaryKeyList();
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
	public List<FieldAnnotation> getPrimaryFieldAnnotationList() {
		return information.getPrimaryFieldAnnotationList();
	}

	@Override
	public List<FieldAnnotation> getNormalFieldAnnotationList() {
		return information.getNormalFieldAnnotationList();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CacheUniqueId that = (CacheUniqueId) o;
		return Objects.equals(getSourceUniqueId(), that.getSourceUniqueId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(sourceUniqueId);
	}

	private String createRedisKeyFormatString(){
		List<String> primaryAddUniqueKeys = new ArrayList<>(information.getPrimaryKeyList());
		primaryAddUniqueKeys.remove(information.getPrimaryKey());
		String primarySharedId = "";
		if (classConfig.getPrimarySharedId() > 0){
			primarySharedId = "." + classConfig.getPrimarySharedId();
		}
		if (primaryAddUniqueKeys.isEmpty()) {
			return "100:%s_" + String.format("%s%s.v%s", classConfig.getName() , primarySharedId, classConfig.getVersionId());
		}
		else {
			String string = StringUtil.join(primaryAddUniqueKeys, ".");
			return "100:%s." + String.format("%s_%s%s.v%s", string, classConfig.getName() , primarySharedId, classConfig.getVersionId());
		}
	}
}
