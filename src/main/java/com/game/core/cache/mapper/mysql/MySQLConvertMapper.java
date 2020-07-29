package com.game.core.cache.mapper.mysql;

import com.game.core.cache.mapper.ValueConvertMapper;
import com.game.core.cache.mapper.ValueConverter;

import java.util.List;

public class MySQLConvertMapper extends ValueConvertMapper {

    @Override
    protected List<ValueConverter<?>> getExtensionConvertList() {
        List<ValueConverter<?>> extensionConvertList = super.getExtensionConvertList();
        return extensionConvertList;
    }
}
