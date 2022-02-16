package com.game.core.cache.source.sql;

import com.game.core.cache.CacheKeyValue;
import com.game.core.cache.ICacheUniqueId;
import jodd.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlProvider {

	private static String SELECT_FORMAT = "SELECT * FROM %s where %s";
	private static String UPSERT_FORMAT = "INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s";
	private static String DELETE_FORMAT = "DELETE FROM %s where %s";

	private final String selectPrimaryCmd;
	private final String selectSecondaryCmd;
	private final String upsertSecondaryCmd;
	private final String deletePrimaryCmd;
	private final String deleteSecondaryCmd;

	public SqlProvider(ICacheUniqueId cacheUniqueId) {
		this.selectPrimaryCmd = createSelectAllCmd(cacheUniqueId);
		this.selectSecondaryCmd = createSelectSecondaryCmd(cacheUniqueId);
		this.upsertSecondaryCmd = createUpsertSecondaryCmd(cacheUniqueId);
		this.deletePrimaryCmd = createDeleteAllCmd(cacheUniqueId);
		this.deleteSecondaryCmd = createDeleteSecondaryCmd(cacheUniqueId);
	}

	public String getSelectPrimaryCmd() {
		return selectPrimaryCmd;
	}

	public String getSelectSecondaryCmd() {
		return selectSecondaryCmd;
	}

	public String getUpsertSecondaryCmd() {
		return upsertSecondaryCmd;
	}

	public String getDeletePrimaryCmd() {
		return deletePrimaryCmd;
	}

	public String getDeleteSecondaryCmd() {
		return deleteSecondaryCmd;
	}

	private String createSelectAllCmd(ICacheUniqueId cacheUniqueId){
		List<CacheKeyValue> keyValueList = createPrimaryKeyValueList(cacheUniqueId);
		return String.format(SELECT_FORMAT,
				cacheUniqueId.getCacheName(),
				whereExpression(keyValueList));
	}

	private String createSelectSecondaryCmd(ICacheUniqueId cacheUniqueId){
		return String.format(SELECT_FORMAT,
				cacheUniqueId.getCacheName(),
				whereExpression(createAllKeyValueList(cacheUniqueId)));
	}

	private String createUpsertSecondaryCmd(ICacheUniqueId cacheUniqueId){
		List<CacheKeyValue> keyValueList = createAllKeyValueList(cacheUniqueId);
		keyValueList.addAll(cacheUniqueId.getOtherNameList().stream().map(name -> new CacheKeyValue(name, "?")).collect(Collectors.toList()));

		List<Object> allKeyList = keyValueList.stream().map(CacheKeyValue::getKey).collect(Collectors.toList());
		List<Object> allValueList = keyValueList.stream().map(CacheKeyValue::getValue).collect(Collectors.toList());
		List<String> duplicateList = cacheUniqueId.getOtherNameList().stream().map(name -> String.format("%s=VALUES(%s)", name, name)).collect(Collectors.toList());

		return String.format(UPSERT_FORMAT,
				cacheUniqueId.getCacheName(),
				StringUtil.join(allKeyList, ", "),
				StringUtil.join(allValueList, ", "),
				StringUtil.join(duplicateList, ", "));
	}

	private String createDeleteAllCmd(ICacheUniqueId cacheUniqueId){
		return String.format(DELETE_FORMAT,
				cacheUniqueId.getCacheName(),
				whereExpression(createAllKeyValueList(cacheUniqueId)));
	}

	private String createDeleteSecondaryCmd(ICacheUniqueId cacheUniqueId){
		return String.format(DELETE_FORMAT,
				cacheUniqueId.getCacheName(),
				whereExpression(createAllKeyValueList(cacheUniqueId)));
	}

	private List<CacheKeyValue> createAllKeyValueList(ICacheUniqueId cacheUniqueId){
		List<CacheKeyValue> keyValueList = createPrimaryKeyValueList(cacheUniqueId);
		keyValueList.addAll(cacheUniqueId.getSecondaryKeyList().stream().map( name-> new CacheKeyValue(name, "?")).collect(Collectors.toList()));
		return keyValueList;
	}

	private List<CacheKeyValue> createPrimaryKeyValueList(ICacheUniqueId cacheUniqueId){
		List<CacheKeyValue> keyValueList = new ArrayList<>();
		keyValueList.add(new CacheKeyValue(cacheUniqueId.getPrimaryKey(), "?"));
		keyValueList.addAll(cacheUniqueId.getAdditionalKeyValueList());
		return keyValueList;
	}

	private String equalExpression(CacheKeyValue keyValue){
		return String.format("%s=%s", keyValue.getKey(), keyValue.getValue());
	}

	private String whereExpression(List<CacheKeyValue> keyValueList){
		List<String> stringList = keyValueList.stream().map(this::equalExpression).collect(Collectors.toList());
		return StringUtil.join(stringList, "AND ");
	}

	@Override
	public String toString() {
		return "{" +
				"selectPrimaryCmd='" + selectPrimaryCmd + '\'' +
				", selectSecondaryCmd='" + selectSecondaryCmd + '\'' +
				", upsertSecondaryCmd='" + upsertSecondaryCmd + '\'' +
				", deletePrimaryCmd='" + deletePrimaryCmd + '\'' +
				", deleteSecondaryCmd='" + deleteSecondaryCmd + '\'' +
				'}';
	}
}
