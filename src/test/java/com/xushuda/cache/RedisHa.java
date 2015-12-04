package com.xushuda.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.unbiz.redis.RedisCacheManager;
import com.xushuda.cache.driver.CacheDriver;
import com.xushuda.cache.model.Info;

/**
 * Redis-Ha client 基于现在的模型，每个key是函数的签名，不相同的值作为不同的field存在HashMap中 <br>
 * flushKey 将某个函数签名下的所有值刷新 <br>
 * flushAll 刷新所有的缓存 <br>
 *                      |-- skey---data
 * 相当于二级key的结构,fkey-|-- skey---data 
 *                      |-- skey---data
 * 
 * @author xushuda
 *
 */
public class RedisHa implements CacheDriver {

    @Resource(name = "innerSitekvRedisMgr")
    private RedisCacheManager innerSitekvRedisMgr;

    /**
     * 最近手动刷新的时间戳<br>
     * 策略是：默认初始值为0,第一次调用get或者getAll会在keyEviction中设置刷新时间为调用时当前的时间戳<br>
     * 之后的调用，只有当expiredTimeStamp大于keyEviction中的值(在上次刷新之后又手动调用flushAll)才会刷新<br>
     * 
     * 可能的问题：比如在后台调用flushAll，但是它被负载均衡到不同的实例上，但是对某数据的请求一直被负载到另一个实例，那么数据无法立即刷新<br>
     * 
     */
    private long expiredTimeStamp;

    /**
     * 现有key以及其刷新时间戳,用keyEvction来保证对一级key的刷新时间的保证
     */
    private Map<String, Long> keyEviction = new HashMap<String, Long>();

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(RedisHa.class);

    private static final String FIXED = "FIXED";

    private static final String KEY_SEPERATOR = ",";

    @Override
    public String id(Object...args) {

        if (args != null) {
            // get the key's prefix
            StringBuilder key = new StringBuilder();
            int pos = 0;
            // 标记ignore列表中的位置
            BitSet ign = new BitSet(args.length);
            for (int i : Info.getAnnotation().getIgnList()) {
                ign.set(i);
            }
            // 将所有非空，并且不在ignore列表中的参数计入key
            for (Object obj : args) {
                if (obj != null && !ign.get(pos++)) {
                    key.append(KEY_SEPERATOR).append(obj.toString());
                }
            }
            // 返回string的key
            String ret = key.toString();
            // empty string will return FIXED
            if (!ret.equals("")) {
                return ret;
            }
            // possible, as all the argument is in ignore list
            logger.debug("the arguments is not null but the key is null @ {}", Info.getSignature().toString());
        }

        return FIXED;

    }

    @Override
    public Object retrieve(String id) {
        String key = Info.getSignature().toString();
        if (shouldFlush(key)) {
            flush(key);
            return null;
        }
        return innerSitekvRedisMgr.hget(key, id);
    }

    @Override
    public void set(String id, Object data, int expiration) {
        String key = Info.getSignature().toString();
        if (innerSitekvRedisMgr.existsKey(key)) {
            innerSitekvRedisMgr.hput(key, id, (Serializable) data);
            // 由于在cached中定义的时间单位是秒，api中是毫秒，所以扩大1000倍
            if (!innerSitekvRedisMgr.extendTime(key, (expiration << 10) & 0x7FFFFFFF)) {
                logger.error("set the expiration time of key {} to {} ,error ", key, expiration);
            }
        } else {
            innerSitekvRedisMgr.hput(key, id, (Serializable) data);
        }
    }

    @Override
    public List<Object> getAll(String[] ids) {
        String key = Info.getSignature().toString();
        if (shouldFlush(key)) {
            flush(key);
            // 返回与key同长度的初始化为null的数组
            return Arrays.asList(new Object[ids.length]);
        }
        return innerSitekvRedisMgr.hmGet(key, ids);
    }

    @Override
    public void setAll(Map<String, Serializable> datas, int expiration) {
        String key = Info.getSignature().toString();
        if (!innerSitekvRedisMgr.existsKey(key)) {
            innerSitekvRedisMgr.hmSet(key, datas);
            if (!innerSitekvRedisMgr.extendTime(key, (expiration << 10) & 0x7FFFFFFF)) {
                logger.error("set the expiration time of key {} to {} ,error ", key, expiration);
            }
        } else {
            innerSitekvRedisMgr.hmSet(key, datas);
        }
    }

    /**
     * if the key does not exist will return false<br>
     * 设置了key的最后刷新时间
     * 
     * @param key
     * @return
     */
    @Override
    public boolean flush(String key) {
        long timeStamp = System.currentTimeMillis();
        logger.info("key: {} is flushed at {}", key, timeStamp);
        keyEviction.put(key, timeStamp);
        // 设置为立即超时
        return innerSitekvRedisMgr.extendTime(key, 0);
    }

    /**
     * O(1) 只会添加一个操作时间戳。真正请求的时候，会刷新缓存
     */
    @Override
    public void flushAll() {
        expiredTimeStamp = System.currentTimeMillis();
    }

    /**
     * 容器重启的时候，这些缓存会刷新 <br>
     * 手动调用flushAll的时候，也会刷新<br>
     * 
     * @param key
     * @return
     */
    private boolean shouldFlush(String key) {
        return !keyEviction.containsKey(key) || expiredTimeStamp > keyEviction.get(key);
    }
}
