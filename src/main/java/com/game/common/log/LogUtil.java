package com.game.common.log;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private static final SerializeConfig serializeConfig = new SerializeConfig(true);

    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static String toJSONString(Object object){
        try {
            return JSONObject.toJSONString(object, serializeConfig, (SerializeFilter)null);
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
