package com.game.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

public class ClassFactory<T> {

	private static final Logger logger = LoggerFactory.getLogger(ClassFactory.class);

	private final String classNamePrefix;

	public ClassFactory(Class<?> baseClass, String leftNamePrefix) {
		this.classNamePrefix = String.format("%s%s", baseClass.getName(), leftNamePrefix);
	}

	@SuppressWarnings("unchecked")
	public Class<T> getClass(String clsNamePostfix) {
		String clsPath = classNamePrefix + clsNamePostfix;
		try {
			return (Class<T>) Class.forName(clsPath);
		}
		catch (Exception e) {
			logger.error("classNamePrefix:{} clsNamePostfix:{}", clsNamePostfix, clsNamePostfix, e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public T createInstance(String classNameSuffix) {
		String className = classNamePrefix + classNameSuffix;
		try {
			Class<T> aClass = (Class<T>) Class.forName(className);
			return createInstance(aClass);
		}
		catch (Exception e) {
			throw new RuntimeException("Class.forName error, className: " + className);
		}
	}

	public T createInstance(Class<T> aClass) {
		return createInstance(aClass, Collections.emptyList());
	}

	public T createInstance(Class<T> aClass, List<Object> parameterList) {
		Class[] aClasses = new Class[parameterList.size()];
		Object[] aClassParams = new Class[parameterList.size()];
		for (int i = 0; i < parameterList.size(); i++) {
			aClassParams[i] = parameterList.get(i);
			aClasses[i] = aClassParams[i].getClass();
		}
		try {
			Constructor<T> ctor = aClass.getDeclaredConstructor(aClasses);
			if (ctor != null) {
				ctor.setAccessible(true);
				return ctor.newInstance(aClassParams);
			}
			else {
				throw new RuntimeException("the constructor's not found, className: " + aClass.getName());
			}
		}
		catch (Exception e) {
			throw new RuntimeException("create failure, className: " + aClass.getName());
		}
	}
}
