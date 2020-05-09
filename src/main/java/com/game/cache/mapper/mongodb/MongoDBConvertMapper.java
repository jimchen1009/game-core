package com.game.cache.mapper.mongodb;

import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.mapper.ValueConverter;

import java.util.List;

public class MongoDBConvertMapper extends ValueConvertMapper {

    @Override
    protected List<ValueConverter<?>> getExtensionConvertList() {
        List<ValueConverter<?>> extensionConvertList = super.getExtensionConvertList();
        return extensionConvertList;
    }
}
