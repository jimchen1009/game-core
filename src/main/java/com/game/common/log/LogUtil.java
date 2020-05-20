package com.game.common.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private static final SerializerFeature[] mySerializerFeatures = new SerializerFeature[] {
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.IgnoreNonFieldGetter
    };

    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static String toJSONString(Object object){
        try {
//            return ToStringBuilder.reflectionToString(object, ToStringStyle.JSON_STYLE);
            return JSON.toJSONString(object, mySerializerFeatures);
        }
        catch (Throwable throwable){
            logger.error("", throwable);
            return toObjectString(object);
        }
    }

    public static String toObjectString(Object object){
        try {
            return object.toString();
        }
        catch (Throwable throwable){
            logger.error("", throwable);
            return object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
        }
    }
}
