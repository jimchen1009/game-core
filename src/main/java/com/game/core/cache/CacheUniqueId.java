package com.game.core.cache;

import com.game.common.util.CommonUtil;
import com.game.core.cache.mapper.ClassAnnotation;
import com.game.core.cache.mapper.FieldAnnotation;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CacheUniqueId implements ICacheUniqueId {

	private static final Logger logger = LoggerFactory.getLogger(CacheUniqueId.class);

	protected final ClassConfig classConfig;
	private final ClassAnnotation information;
	private final List<CacheKeyValue> additionalKeyValueList;
	protected String redisPrimaryKeyFormatString;

	public CacheUniqueId(ClassConfig classConfig, List<CacheKeyValue> additionalKeyValueList) {
		this.classConfig = classConfig;
		this.information = ClassAnnotation.create(classConfig.getAClass());
		List<String> additionalKeyList = information.getAdditionalKeyList();
		this.additionalKeyValueList = new ArrayList<>(additionalKeyValueList.size());
		for (String additionalKey : additionalKeyList) {
			CacheKeyValue oneUtilOkay = CommonUtil.findOneUtilOkay(additionalKeyValueList, findOne -> findOne.getKey() == additionalKey);
			this.additionalKeyValueList.add(Objects.requireNonNull(oneUtilOkay));
		}
		this.redisPrimaryKeyFormatString = createRedisPrimaryKeyFormatString();
	}

	@Override
	public List<CacheKeyValue> createPrimaryAndAdditionalKeys(long primaryKey) {
		List<CacheKeyValue> cacheKeyValueList = new ArrayList<>(additionalKeyValueList.size() + 1);
		cacheKeyValueList.add(new CacheKeyValue(getPrimaryKey(), primaryKey));
		cacheKeyValueList.addAll(additionalKeyValueList);
		return cacheKeyValueList;
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
	public String getPrimaryKey() {
		return information.getPrimaryKey();
	}

	@Override
	public List<String> getAdditionalKeyList() {
		return information.getAdditionalKeyList();
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
		return Objects.equals(additionalKeyValueList, that.additionalKeyValueList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}

	private String createRedisPrimaryKeyFormatString(){
		if (additionalKeyValueList.isEmpty()){
			String string = StringUtil.join(additionalKeyValueList.stream().map(CacheKeyValue::getValue).collect(Collectors.toList()), "_");
			return "100:%s_" + String.format("%s.%s.v%s", classConfig.getName() , string, classConfig.getVersionId());
		}
		else {
			return "100:%s_" + String.format("%s.v%s", classConfig.getName() , classConfig.getVersionId());
		}
	}
}
