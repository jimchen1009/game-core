package com.game.core.cache.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段解析器~
 */
public class ValueConvertMapper {

    private static final Logger logger = LoggerFactory.getLogger(ValueConvertMapper.class);

    private final JsonValueConverter defaultConverter;
    private final Map<String, ValueConverter<?>> convertMap;

    public ValueConvertMapper() {
        this.defaultConverter = new JsonValueConverter();
        this.convertMap = new ConcurrentHashMap<>();
        this.initializeInternalConvert();
    }

    private void initializeInternalConvert(){
        add(new IntegerValueConverter());
        add(new LongValueConverter());
        add(new ShortValueConverter());
        add(new ByteValueConverter());
        add(new StringValueConverter());
        List<ValueConverter<?>> extensionConvertList = getExtensionConvertList();
        for (ValueConverter<?> valueConverter : extensionConvertList) {
            add(valueConverter);
        }
    }

    protected List<ValueConverter<?>> getExtensionConvertList(){
        return new ArrayList<>();
    }

    public void add(ValueConverter<?> convert){
        String name = convert.getGenericClass().getName();
        convertMap.put(name, convert);
        if (convert.getGenericClass().equals(Long.class)){
            convertMap.put(Long.TYPE.getName(), convert);
        }
        else if (convert.getGenericClass().equals(Short.class)){
            convertMap.put(Short.TYPE.getName(), convert);
        }
        else if (convert.getGenericClass().equals(Integer.class)){
            convertMap.put(Integer.TYPE.getName(), convert);
        }
        else if (convert.getGenericClass().equals(Byte.class)){
            convertMap.put(Byte.TYPE.getName(), convert);
        }
    }

    public JsonValueConverter getDefault() {
        return defaultConverter;
    }

    @SuppressWarnings("unchecked")
    public <T> ValueConverter<T> get(Class<T> cls){
       return (ValueConverter<T>)convertMap.get(cls.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> ValueConverter<T> getOrDefault(Class<T> cls){
        return (ValueConverter<T>)convertMap.getOrDefault(cls.getName(), defaultConverter);
    }
}
