package com.game.common.log;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static String toJSONString(Object object){
        try {
            return ToStringBuilder.reflectionToString(object, ToStringStyle.JSON_STYLE);
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
