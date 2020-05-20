package com.game.cache.mapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonValueConverter extends ValueConverter<Object> {

    private static final SerializerFeature[] mySerializerFeatures = new SerializerFeature[] {
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.IgnoreNonFieldGetter,
            SerializerFeature.WriteClassName,
            SerializerFeature.SkipTransientField,
    };

    private static final ParserConfig myParserConfig = new ParserConfig();
    static {
        myParserConfig.setAutoTypeSupport(true);
    }

    public JsonValueConverter() {
        super(null, "");
    }

    @Override
    protected Object decode0(Object cacheValue) {
        return parse(String.valueOf(cacheValue));
    }

    @Override
    protected Object encode0(Object dataValue) {
        return toJSONString(dataValue);
    }

    public static Object parse(String string){
        return JSON.parse(string, myParserConfig);
    }

    public static String toJSONString(Object object){
        return JSON.toJSONString(object, mySerializerFeatures);
    }
}
