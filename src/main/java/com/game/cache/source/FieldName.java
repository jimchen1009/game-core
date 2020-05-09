package com.game.cache.source;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class FieldName {

    public static final String EXPIRED = "1";

    public static final String UPDATE = "2";

    public static final String DELETE = "3";

    public static final String CACHE = "4";


    public static final Collection<String> SpecialNames = Collections.unmodifiableList(Arrays.asList(EXPIRED, UPDATE, CACHE));
}
