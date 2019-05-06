package com.mishaki.sputil;


import android.app.Application;

import com.mishaki.sputil.annotation.SpValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class ParseUtil {
    static List<EntityInfo> parseEntity(Object entity, boolean isGet) {
        List<EntityInfo> list = new ArrayList<>();
        scanField(entity, entity.getClass(), list, isGet);
        return list;
    }

    private static void scanField(Object entity, Class clazz, List<EntityInfo> list, boolean isGet) {
        if (clazz == Object.class) {
            return;
        }
        scanField(entity, clazz.getSuperclass(), list, isGet);
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            int modify = field.getModifiers();
            Boolean result = !Modifier.isTransient(modify) &&
                    !Modifier.isStatic(modify) &&
                    Modifier.isPublic(modify);
            if (result) {
                try {
                    Class fieldType = field.getType();
                    EntityInfo parseInfo = new EntityInfo();
                    parseInfo.type = getType(fieldType);
                    if (isGet) {
                        parseInfo.field = field;
                    }
                    SpValue value = field.getAnnotation(SpValue.class);
                    if (value != null && !value.key().isEmpty()) {
                        parseInfo.key = value.key();
                    } else {
                        parseInfo.key = field.getName();
                    }
                    parseInfo.value = String.valueOf(field.get(entity));
                    list.add(parseInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static String getType(Class clazz) {
        if (clazz == int.class || clazz == Integer.class) {//如果是int
            return EntityInfo.INT;
        } else if (clazz == long.class || clazz == Long.class) {//如果是long
            return EntityInfo.LONG;
        } else if (clazz == boolean.class || clazz == Boolean.class) {//如果是boolean
            return EntityInfo.BOOLEAN;
        } else if (clazz == float.class || clazz == Float.class) {//如果是float
            return EntityInfo.FLOAT;
        } else {//否则都视为String
            return EntityInfo.STRING;
        }
    }

    static Application getApplication() {
        try {
            Class activityThreadClazz = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThreadClazz.getMethod("currentActivityThread");
            Object activityThread = currentActivityThread.invoke(null);
            Field mInitialApplicationField = activityThreadClazz.getDeclaredField("mInitialApplication");
            mInitialApplicationField.setAccessible(true);
            return (Application) mInitialApplicationField.get(activityThread);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}