package com.game.common.util;

public interface IEnumBase {

	int getId();

	String name();


	static <T extends IEnumBase> T findOne(T[] enumBases, int id){
		for (T enumBase : enumBases) {
			if (enumBase.getId() == id) {
				return enumBase;
			}
		}
		return null;
	}

	static <T extends IEnumBase> T findOne(T[] enumBases, String name){
		for (T enumBase : enumBases) {
			if (enumBase.name().equals(name)) {
				return enumBase;
			}
		}
		return null;
	}
}
