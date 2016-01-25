package com.baidu.aenhancer.conf.runtime;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.exception.CodingError;

/**
 * 
 * 将Configurable类的“namespace”和各个config文件的“字段名”结合，影射为properties文件中的字段<br>
 * properties中的字段可以使用*通配符来设置配置
 * 
 * 创建实现Configurable的类，这个类不是工厂，它只关心对象是怎么配置的，不关心对象创建的规则。<br>
 * 具体对象怎么创建怎么维护还是由Factory类来实现，所以工厂类只需关心对象的生命周期等
 * 
 * 
 * 
 * @author xushuda
 *
 */
public class RuntimeConfigManager {

    private final static Logger logger = LoggerFactory.getLogger(RuntimeConfigManager.class);
    private static final String DEFAULT_CONFIG_FILE_Name = "/enhancer.properties";
    private static final ConcurrentHashMap<String, ConfigSet> fileConfMap = new ConcurrentHashMap<String, ConfigSet>();
    static {

    }

    /**
     * 是否有失败
     * 
     * @return true
     */
    public static boolean yieldAll() {
        for (ConfigSet config : fileConfMap.values()) {
            try {
                config.yield();
            } catch (CodingError e) {
                logger.error("error when yield ", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 是否有失败
     * 
     * @param fileName
     */
    public static boolean yield(String fileName) {
        ConfigSet config = fileConfMap.get(fileName);
        if (null != config) {
            try {
                config.yield();
            } catch (CodingError e) {
                logger.error("error when yield ", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param confCls
     * @param namespace
     * @return
     * @throws CodingError
     */
    private static <CONF> CONF getConfObject(Class<CONF> confCls, Map<String, Pattern> patterns, Properties props,
            String namespace) throws CodingError {
        try {
            Constructor<CONF> cons = confCls.getConstructor();
            cons.setAccessible(true);
            CONF config = cons.newInstance();
            for (Field field : confCls.getDeclaredFields()) {
                field.setAccessible(true);
                String propName = namespace + "." + field.getName();
                // 先完全匹配
                if (patterns.containsKey(propName)) {
                    fieldSet(field, props, propName, config);
                } else {
                    // 没有再*匹配
                    for (Entry<String, Pattern> kv : patterns.entrySet()) {
                        if (kv.getValue().matcher(propName).find()) {
                            fieldSet(field, props, kv.getKey(), config);
                            break;
                        }
                    }
                    // 如果都没有匹配，就是默认值
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
    public static <CONF, OBJ extends Configurable<CONF>> OBJ factory(Class<OBJ> clazz, Object...p) throws CodingError {
        return factory(DEFAULT_CONFIG_FILE_Name, clazz, p);
    }

    /**
     * 
     * @param clazz
     * @param p
     * @return
     * @throws CodingError
     */
    @SuppressWarnings("unchecked")
    public static <CONF, OBJ extends Configurable<CONF>> OBJ factory(String fileName, Class<OBJ> clazz, Object...p)
            throws CodingError {
        OBJ ret = null;
        // 由Configurable类获取泛型的配置类
        Class<CONF> confCls = null;
        Type[] types = clazz.getGenericInterfaces();
        for (Type t : types) {
            if (t instanceof ParameterizedType
                    && Configurable.class.isAssignableFrom((Class<?>) (((ParameterizedType) t).getRawType()))) {
                Type[] args = ((ParameterizedType) t).getActualTypeArguments();
                if (args[0] instanceof Class) {
                    confCls = (Class<CONF>) args[0]; // TODO [0]
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
            ConfigSet configs = getConfSet(fileName);
            // get config obj
            CONF config = getConfObject(confCls, configs.getPatterns(), configs.getProperties(), ret.namespace());
            // config
            ret.config(config);
            // attach
            configs.attach(ret);
            // return
            return ret;
        } catch (Exception e) {
            throw new CodingError("error read the config", e);
        }
    }

    private static ConfigSet getConfSet(String fileName) {
        if (null == fileConfMap.get(fileName)) {
            fileConfMap.putIfAbsent(fileName, new ConfigSet(fileName));
        }
        return fileConfMap.get(fileName);
    }

    /**
     * 
     * @param from
     * @return
     */
    private static Class<?> checkPrimitive(Class<?> from) {
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
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     */
    private static void fieldSet(Field field, Properties props, String propName, Object confObj)
            throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
        Class<?> from = field.getType();
        if (from == Integer.class || from == int.class) {
            field.setInt(
                    confObj,
                    Integer.valueOf(props.getProperty(propName)) == null ? 0 : Integer.valueOf(props
                            .getProperty(propName)));
        } else if (from == Boolean.class || from == boolean.class) {
            field.setBoolean(
                    confObj,
                    Boolean.valueOf(props.getProperty(propName)) == null ? true : Boolean.valueOf(props
                            .getProperty(propName)));
        } else if (from == Double.class || from == double.class) {
            field.setDouble(
                    confObj,
                    Double.valueOf(props.getProperty(propName)) == null ? 0.0 : Double.valueOf(props
                            .getProperty(propName)));
        } else if (from == Short.class || from == short.class) {
            field.setShort(confObj,
                    Short.valueOf(props.getProperty(propName)) == null ? 0 : Short.valueOf(props.getProperty(propName)));
        } else if (from == Long.class || from == long.class) {
            field.setLong(confObj,
                    Long.valueOf(props.getProperty(propName)) == null ? 0 : Long.valueOf(props.getProperty(propName)));
        } else if (from == Byte.class || from == byte.class) {
            field.setByte(confObj,
                    Byte.valueOf(props.getProperty(propName)) == null ? 0 : Byte.valueOf(props.getProperty(propName)));
        } else if (from == Float.class || from == float.class) {
            field.setFloat(confObj,
                    Float.valueOf(props.getProperty(propName)) == null ? 0 : Float.valueOf(props.getProperty(propName)));
        } else if (from == Character.class || from == char.class) {
            field.setChar(confObj, props.getProperty(propName).charAt(0));
        }
        Object newValue = field.get(confObj);
        logger.info("field :{} is set to {}, key={}", field.getName(), newValue, propName);
    }

    /**
     * 一个配置文件对应的类
     * 
     * @author xushuda
     *
     */
    static class ConfigSet {
        private volatile Properties props;
        private volatile Map<String, Pattern> patterns;
        private CopyOnWriteArrayList<Configurable<?>> attachments;
        private final String fileName;

        private void init(Properties props, Map<String, Pattern> patterns, String fileName) {
            InputStream is = null;
            try {
                is = RuntimeConfigManager.class.getResourceAsStream(fileName);
                props.load(is);
                Enumeration<?> e = props.propertyNames();
                while (e.hasMoreElements()) {
                    String key = String.class.cast(e.nextElement());
                    patterns.put(key, Pattern.compile(key));
                }
            } catch (IOException e1) {
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void yield() throws CodingError {
            Properties props = new Properties();
            Map<String, Pattern> patterns = new HashMap<String, Pattern>();
            init(props, patterns, fileName);
            this.patterns = patterns;
            this.props = props;
            for (Configurable configTarget : attachments) {
                Object config =
                        getConfObject(configTarget.getConfig().getClass(), patterns, props, configTarget.namespace());
                configTarget.config(config);
            }
        }

        public ConfigSet(String fileName) {
            this.fileName = fileName;
            props = new Properties();
            patterns = new HashMap<String, Pattern>();
            attachments = new CopyOnWriteArrayList<Configurable<?>>();
            init(props, patterns, fileName);
        }

        synchronized public void attach(Configurable<?> attachment) {
            attachments.add(attachment);
        }

        public Properties getProperties() {
            return props;
        }

        public Map<String, Pattern> getPatterns() {
            return patterns;
        }
    }
}
