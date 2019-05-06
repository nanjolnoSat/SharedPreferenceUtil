package com.mishaki.sputil;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mishaki.sputil.annotation.SpGet;
import com.mishaki.sputil.annotation.SpKey;
import com.mishaki.sputil.annotation.SpPrefix;
import com.mishaki.sputil.annotation.SpSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public abstract class BaseSharedPreferencesUtil {
    private String spName;
    /*private Application context = Reflect.on("android.app.ActivityThread")
            .call("currentActivityThread")
            .field("mInitialApplication").get();*/
    private Application context = ParseUtil.getApplication();

    public BaseSharedPreferencesUtil() {
        spName = getSpName();
        Log.v("BaseSharedPreferencesUtilMsg","context:" + context);
    }

    protected abstract String getSpName();

    public void put(Object entity) {
        SpPrefix spPrefix = entity.getClass().getAnnotation(SpPrefix.class);
        String prefix = "";
        if (spPrefix != null) {
            prefix = spPrefix.value();
        }
        SharedPreferences.Editor editor = getEditor();
        List<EntityInfo> list = ParseUtil.parseEntity(entity, false);
        for (EntityInfo parseInfo : list) {
            switch (parseInfo.type) {
                case EntityInfo.INT:
                    editor.putInt(prefix + parseInfo.key, Integer.parseInt(parseInfo.value));
                    break;
                case EntityInfo.LONG:
                    editor.putLong(prefix + parseInfo.key, Long.parseLong(parseInfo.value));
                    break;
                case EntityInfo.FLOAT:
                    editor.putFloat(prefix + parseInfo.key, Float.parseFloat(parseInfo.value));
                    break;
                case EntityInfo.BOOLEAN:
                    editor.putBoolean(prefix + parseInfo.key, Boolean.parseBoolean(parseInfo.value));
                    break;
                default:
                    editor.putString(prefix + parseInfo.key, parseInfo.value);
                    break;
            }
        }
        editor.apply();
    }

    public void get(Object entity) {
        SharedPreferences sp = getSp();
        List<EntityInfo> list = ParseUtil.parseEntity(entity, true);
        for (EntityInfo entityInfo : list) {
            try {
                switch (entityInfo.type) {
                    case EntityInfo.INT:
                        int intValue = sp.getInt(entityInfo.key, Integer.parseInt(entityInfo.value));
                        entityInfo.field.set(entity, intValue);
                        break;
                    case EntityInfo.LONG:
                        long longValue = sp.getLong(entityInfo.key, Long.parseLong(entityInfo.value));
                        entityInfo.field.set(entity, longValue);
                        break;
                    case EntityInfo.FLOAT:
                        float floatValue = sp.getFloat(entityInfo.key, Float.parseFloat(entityInfo.value));
                        entityInfo.field.set(entity, floatValue);
                        break;
                    case EntityInfo.BOOLEAN:
                        boolean booleanValue = sp.getBoolean(entityInfo.key, Boolean.parseBoolean(entityInfo.value));
                        entityInfo.field.set(entity, booleanValue);
                        break;
                    default:
                        String stringValue = sp.getString(entityInfo.key, entityInfo.value);
                        entityInfo.field.set(entity, stringValue);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T get(Class<T> clazz) {
        T entity = null;
        try {
            entity = clazz.newInstance();
            get(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    public <T> T getControlObject(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                SpGet spGet = method.getAnnotation(SpGet.class);
                SpSet spPut = method.getAnnotation(SpSet.class);
                if (spGet == null && spPut == null) {
                    return null;
                }
                if (spGet != null) {
                    String key = spGet.value();
                    if (key.isEmpty()) {
                        return null;
                    }
                    String type = ParseUtil.getType(method.getReturnType());
                    boolean hasDefaultValue = args != null && args.length != 0;
                    switch (type) {
                        case EntityInfo.INT:
                            int intDefault = 0;
                            if (hasDefaultValue) {
                                if (args[0] != null) {
                                    intDefault = Integer.parseInt(String.valueOf(args[0]));
                                }
                            }
                            return getSp().getInt(key, intDefault);
                        case EntityInfo.LONG:
                            long longDefault = 0L;
                            if (hasDefaultValue) {
                                if (args[0] != null) {
                                    longDefault = Long.parseLong(String.valueOf(args[0]));
                                }
                            }
                            return getSp().getLong(key, longDefault);
                        case EntityInfo.FLOAT:
                            float floatDefault = 0f;
                            if (hasDefaultValue) {
                                if (args[0] != null) {
                                    floatDefault = Float.parseFloat(String.valueOf(args[0]));
                                }
                            }
                            return getSp().getFloat(key, floatDefault);
                        case EntityInfo.BOOLEAN:
                            boolean booleanDefault = false;
                            if (hasDefaultValue) {
                                if (args[0] != null) {
                                    booleanDefault = Boolean.parseBoolean(String.valueOf(args[0]));
                                }
                            }
                            return getSp().getBoolean(key, booleanDefault);
                        default:
                            String stringDefault = "";
                            if (hasDefaultValue) {
                                if (args[0] != null) {
                                    stringDefault = String.valueOf(args[0]);
                                }
                            }
                            return getSp().getString(key, stringDefault);
                    }
                } else {
                    if (args == null || args.length == 0) {
                        return null;
                    }
                    SharedPreferences.Editor editor = getEditor();
                    Class[] types = method.getParameterTypes();
                    for (int i = 0; i < args.length; i++) {
                        SpKey spMethodValue = null;
                        for (Annotation annotation : method.getParameterAnnotations()[i]) {
                            if (annotation.annotationType() == SpKey.class) {
                                spMethodValue = (SpKey) annotation;
                            }
                        }
                        String key;
                        if (spMethodValue != null) {
                            key = spMethodValue.value();
                        } else {
                            continue;
                        }
                        String type = ParseUtil.getType(types[i]);
                        switch (type) {
                            case EntityInfo.INT:
                                int intDefault = Integer.parseInt(String.valueOf(args[i]));
                                editor.putInt(key, intDefault);
                                break;
                            case EntityInfo.LONG:
                                long longDefault = Long.parseLong(String.valueOf(args[i]));
                                editor.putLong(key, longDefault);
                                break;
                            case EntityInfo.FLOAT:
                                float floatDefault = Float.parseFloat(String.valueOf(args[i]));
                                editor.putFloat(key, floatDefault);
                                break;
                            case EntityInfo.BOOLEAN:
                                boolean booleanDefault = Boolean.parseBoolean(String.valueOf(args[i]));
                                editor.putBoolean(key, booleanDefault);
                                break;
                            default:
                                String stringDefault = String.valueOf(args[i]);
                                editor.putString(key, stringDefault);
                                break;
                        }
                    }
                    editor.apply();
                    return proxy;
                }
            }
        });
    }

    public BaseSharedPreferencesUtil putInt(String key, int value) {
        getEditor().putInt(key, value).apply();
        return this;
    }

    public BaseSharedPreferencesUtil putLong(String key, long value) {
        getEditor().putLong(key, value).apply();
        return this;
    }

    public BaseSharedPreferencesUtil putFloat(String key, float value) {
        getEditor().putFloat(key, value).apply();
        return this;
    }

    public BaseSharedPreferencesUtil putBoolean(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
        return this;
    }

    public BaseSharedPreferencesUtil putString(String key, String value) {
        getEditor().putString(key, value).apply();
        return this;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return getSp().getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return getSp().getLong(key, defaultValue);
    }

    public float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(String key, float defaultValue) {
        return getSp().getFloat(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getSp().getBoolean(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return getSp().getString(key, defaultValue);
    }

    public void clear() {
        getEditor().clear().apply();
    }

    public void remove(String key) {
        getEditor().remove(key).apply();
    }

    public boolean contains(String key) {
        return getSp().contains(key);
    }

    public SharedPreferences getSp() {
        return context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    protected SharedPreferences.Editor getEditor() {
        return getSp().edit();
    }
}