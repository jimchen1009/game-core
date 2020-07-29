package com.game.core.cache.mapper.redis;

import com.game.core.cache.mapper.ValueConvertMapper;
import com.game.core.cache.mapper.ValueConverter;

import java.util.List;

public class RedisConvertMapper extends ValueConvertMapper {

    @Override
    protected List<ValueConverter<?>> getExtensionConvertList() {
        List<ValueConverter<?>> extensionConvertList = super.getExtensionConvertList();
        extensionConvertList.add(new RedisByteConverter());
        extensionConvertList.add(new RedisIntegerConverter());
        extensionConvertList.add(new RedisLongConverter());
        extensionConvertList.add(new RedisShortConverter());
        return extensionConvertList;
    }
}
