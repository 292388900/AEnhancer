package com.baidu.aenhancer.core.conf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import com.baidu.aenhancer.exception.CodingError;

/**
 * 可以使用通配符来设置配置字段
 * 
 * 实现Configurable的类
 * 
 * 
 * 
 * @author xushuda
 *
 */
public class ConfigManager {

    private static ConfigManager instance;

    private Properties props;
    private Map<String, Pattern> propPatterns;

    /**
     * 
     * @throws IOException
     */
    private ConfigManager() throws IOException {
        props = new Properties();
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream("/enhancer.properties");
            props.load(is);
            propPatterns = new HashMap<String, Pattern>();
            Enumeration<?> e = props.propertyNames();
            while (e.hasMoreElements()) {
                String key = String.class.cast(e.nextElement());
                propPatterns.put(key, Pattern.compile(key));
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * 获取单例
     * 
     * @return
     * @throws IOException
     */
    public static ConfigManager getInstance() throws IOException {
        if (null == instance) {
            synchronized (ConfigManager.class) {
                if (null == instance) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    /**
     * 
     * @param confCls
     * @param id
     * @return
     * @throws CodingError
     */
    public <CONF> CONF getConfObject(Class<CONF> confCls, String id) throws CodingError {
        try {
            Constructor<CONF> cons = confCls.getConstructor();
            cons.setAccessible(true);
            CONF config = cons.newInstance();
            for (Field field : confCls.getDeclaredFields()) {
                field.setAccessible(true);
                String propName = id + "." + field.getName();
                if (propPatterns.containsKey(propName)) {
                    fieldSet(field, propName, config);
                } else {
                    for (Entry<String, Pattern> kv : propPatterns.entrySet()) {
                        if (kv.getValue().matcher(propName).find()) {
                            fieldSet(field, kv.getKey(), config);
                            break;
                        }
                    }
                    // 如果没有设置，就是默认值
                }
            }
            return config;
        } catch (Exception e) {
            throw new CodingError("error read the config", e);
        }
    }

    /**
     * 
     * @param clazz
     * @param p
     * @return
     * @throws CodingError
     */
    @SuppressWarnings("unchecked")
    public <CONF, OBJ extends Configurable<CONF>> OBJ factory(Class<OBJ> clazz, Object...p) throws CodingError {
        OBJ ret = null;
        Class<CONF> confCls = null;
        Type[] types = clazz.getGenericInterfaces();
        for (Type t : types) {
            if (t instanceof ParameterizedType
                    && Configurable.class.isAssignableFrom((Class<?>) (((ParameterizedType) t).getRawType()))) {
                Type[] args = ((ParameterizedType) t).getActualTypeArguments();
                if (args[0] instanceof Class) {
                    confCls = (Class<CONF>) args[0];
                    break;
                }
            }
        }
        if (null == confCls) {
            throw new CodingError("the config cls is null");
        }
        try {
            if (p != null) {
                Class<?>[] parameterTypes = new Class<?>[p.length];
                int i = 0;
                for (Object obj : p) {
                    parameterTypes[i++] = checkPrimitive(obj.getClass());
                }
                ret = clazz.getConstructor(parameterTypes).newInstance(p);
            } else {
                ret = clazz.newInstance();
            }
            // get config obj
            CONF config = getConfObject(confCls, ret.namespace());
            // config
            ret.config(config);
            return ret;
        } catch (Exception e) {
            throw new CodingError("error read the config", e);
        }
    }

    /**
     * 
     * @param from
     * @return
     */
    private Class<?> checkPrimitive(Class<?> from) {
        if (from.isPrimitive()) {
            if (from == Integer.class) {
                return int.class;
            } else if (from == Boolean.class) {
                return boolean.class;
            } else if (from == Double.class) {
                return double.class;
            } else if (from == Short.class) {
                return short.class;
            } else if (from == Long.class) {
                return long.class;
            } else if (from == Byte.class) {
                return byte.class;
            } else if (from == Character.class) {
                return char.class;
            } else if (from == Float.class) {
                return float.class;
            }
        }
        return from;
    }

    /**
     * 
     * @param field
     * @param propName
     * @param confObj
     */
    private void fieldSet(Field field, String propName, Object confObj) {
        System.out.println(propName); // TODO complete
    }
}
