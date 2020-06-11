package com.game.cache.mapper;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ValueConverterRunner {

    private static final Logger logger = LoggerFactory.getLogger(ValueConverterRunner.class);

    @Test
    public void run(){
        ValueConvertMapper mapper = new ValueConvertMapper();
        JsonClass jsonClass = new JsonClass(0, Arrays.asList(0, 2, 4), "Jim");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("int", Integer.MAX_VALUE);
        hashMap.put("long", Long.MAX_VALUE);
        hashMap.put("string", "String");
        Object[] objects = new Object[]{ 2, 3L, (short)4, "Jim", new Date(),  Arrays.asList(Long.MAX_VALUE, 2, 4), hashMap, jsonClass};
        for (Object object : objects) {
            ValueConverter<?> convert = mapper.getOrDefault(object.getClass());
            Object encode = convert.encode(object);
            Object decode = convert.decode(encode);
            logger.debug("{} encode: {} decode: {}", object, encode, decode);
        }
    }

    public static class JsonClass{
        public int id = 0;
        public List<Integer> idList;
        public List<String> emptyList;
        public String name = "Jim";
        public String nullString;

        public JsonClass() {
        }

        public JsonClass(int id, List<Integer> idList, String name) {
            this.id = id;
            this.idList = idList;
            this.name = name;
        }

        @Override
        public String toString() {
            return "{" +
                    "id=" + id +
                    ", idList=" + idList +
                    ", emptyList=" + emptyList +
                    ", name='" + name + '\'' +
                    ", nullString='" + nullString + '\'' +
                    '}';
        }
    }
}
