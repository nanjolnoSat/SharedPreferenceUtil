package com.mishaki.sputil;

import java.lang.reflect.Field;

class EntityInfo {
    static final String INT = "int";
    static final String LONG = "long";
    static final String BOOLEAN = "boolean";
    static final String FLOAT = "float";
    static final String STRING = "string";

    String type;
    String key;
    String value;

    Field field;
}
