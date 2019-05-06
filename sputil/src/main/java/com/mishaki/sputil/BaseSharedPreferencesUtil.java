package com.mishaki.sputil;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

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
    private Application context = ParseUtil.getApplication();

    public BaseSharedPreferencesUtil() {
        spName = getSpName();
    }

    protected abstract String getSpName();

    /**
     * 将对象里面所有的带有public修饰符,并且没有static和transient修饰符<br/>
     * 的字段报错到sp里面,可以在该字段上面贴SpValue的注解来修改key值<br/>
     * 可以在class面贴SpPrefix注解给该class里面所有的key加个前缀
     */
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

    /**
     * 通过Class里面的字段名称获取对应的key值<br/>
     * 并将相应的数据设置到该对象<br/>
     * 如果该对象某个参数有值,该值将作为当key不存在时的默认值
     */
    public void get(Object entity) {
        SharedPreferences sp = getSp();
        List<EntityInfo> list = ParseUtil.parseEntity(entity, true);
        SpPrefix spPrefix = entity.getClass().getAnnotation(SpPrefix.class);
        String prefix = "";
        if(spPrefix != null) {
            prefix = spPrefix.value();
        }
        for (EntityInfo entityInfo : list) {
            try {
                switch (entityInfo.type) {
                    case EntityInfo.INT:
                        int intValue = sp.getInt(prefix + entityInfo.key, Integer.parseInt(entityInfo.value));
                        entityInfo.field.set(entity, intValue);
                        break;
                    case EntityInfo.LONG:
                        long longValue = sp.getLong(prefix + entityInfo.key, Long.parseLong(entityInfo.value));
                        entityInfo.field.set(entity, longValue);
                        break;
                    case EntityInfo.FLOAT:
                        float floatValue = sp.getFloat(prefix + entityInfo.key, Float.parseFloat(entityInfo.value));
                        entityInfo.field.set(entity, floatValue);
                        break;
                    case EntityInfo.BOOLEAN:
                        boolean booleanValue = sp.getBoolean(prefix + entityInfo.key, Boolean.parseBoolean(entityInfo.value));
                        entityInfo.field.set(entity, booleanValue);
                        break;
                    default:
                        String stringValue = sp.getString(prefix + entityInfo.key, entityInfo.value);
                        entityInfo.field.set(entity, stringValue);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 通过Class里面的字段名称获取对应的key值<br/>
     * 并封装到一个对象,再返回,该Class必须拥有一个无参数的构造方法
     */
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

    /**
     * 使用代理的方式获取控制对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getControlObject(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                SpGet spGet = method.getAnnotation(SpGet.class);
                SpSet spPut = method.getAnnotation(SpSet.class);
                //必须有其中的一个注解
                if (spGet == null && spPut == null) {
                    return null;
                }
                //当为Get模式的时候
                if (spGet != null) {
                    //必须拥有key
                    String key = spGet.value();
                    if (key.isEmpty()) {
                        return null;
                    }
                    //获取返回类型
                    String type = ParseUtil.getType(method.getReturnType());
                    //该方法是否拥有参数
                    boolean hasDefaultValue = args != null && args.length != 0;
                    switch (type) {
                        case EntityInfo.INT:
                            int intDefault = 0;
                            if (hasDefaultValue) {
                                if (args[0] != null) {
                                    //当有参数的时候,拿该参数的值作为默认值
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
                            //当不是上面的4种类型的时候,就视为String
                            String stringDefault = "";
                            if (hasDefaultValue) {
                                if (args[0] != null) {
                                    stringDefault = String.valueOf(args[0]);
                                }
                            }
                            return getSp().getString(key, stringDefault);
                    }
                } else {//当为Set模式的时候
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
                                break;
                            }
                        }
                        String key;
                        //必须拥有key,否则不继续执行
                        if (spMethodValue != null) {
                            key = spMethodValue.value();
                        } else {
                            continue;
                        }
                        String type = ParseUtil.getType(types[i]);
                        switch (type) {
                            case EntityInfo.INT:
                                int intValue = Integer.parseInt(String.valueOf(args[i]));
                                editor.putInt(key, intValue);
                                break;
                            case EntityInfo.LONG:
                                long longValue = Long.parseLong(String.valueOf(args[i]));
                                editor.putLong(key, longValue);
                                break;
                            case EntityInfo.FLOAT:
                                float floatValue = Float.parseFloat(String.valueOf(args[i]));
                                editor.putFloat(key, floatValue);
                                break;
                            case EntityInfo.BOOLEAN:
                                boolean booleanValue = Boolean.parseBoolean(String.valueOf(args[i]));
                                editor.putBoolean(key, booleanValue);
                                break;
                            default:
                                String stringValue = String.valueOf(args[i]);
                                editor.putString(key, stringValue);
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

    /**
     * 获取SharedPreference对象
     */
    public SharedPreferences getSp() {
        return context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    /**
     * 获取Editor对象
     */
    protected SharedPreferences.Editor getEditor() {
        return getSp().edit();
    }
}