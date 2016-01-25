package com.baidu.aenhancer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.ext.impl.aggr.CacheDriver;
import com.baidu.unbiz.redis.RedisCacheManager;

/**
 * Redis-Ha client 基于现在的模型，每个key是函数的签名，不相同的值作为不同的field存在HashMap中 <br>
 * flushKey 将某个函数签名下的所有值刷新 <br>
 * flushAll 刷新所有的缓存 <br>
 * 
 * <pre>
 * 相当于二级key的结构:
 *            |-- key1:data
 * keySpace1->|-- key2:data 
 *            |-- key3:data
 *            |...
 * keySpace2->|-- key1:data
 * @author xushuda
 *
 */
public class RedisHa implements CacheDriver {

    // @Resource(name = "innerSitekvRedisMgr")
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

    @Override
    public Object get(String key, String keySpace) {
        if (shouldFlush(keySpace)) {
            flush(keySpace);
            return null;
        }
        return innerSitekvRedisMgr.hget(keySpace, key);
    }

    @Override
    public void set(String key, Object data, int expiration, String keySpace) {
        if (innerSitekvRedisMgr.existsKey(keySpace)) {
            innerSitekvRedisMgr.hput(keySpace, key, (Serializable) data);
            // 由于在cached中定义的时间单位是秒，api中是毫秒，所以扩大1000倍
            if (!innerSitekvRedisMgr.extendTime(keySpace, (expiration << 10) & 0x7FFFFFFF)) {
                logger.error("set the expiration time of key {} to {} ,error ", keySpace, expiration);
            }
        } else {
            innerSitekvRedisMgr.hput(keySpace, key, (Serializable) data);
        }
    }

    @Override
    public List<Object> getAll(List<String> keys, String keySpace) {
        if (shouldFlush(keySpace)) {
            flush(keySpace);
            // 返回与key同长度的初始化为null的数组
            return Arrays.asList(new Object[keys.size()]);
        }
        return innerSitekvRedisMgr.hmGet(keySpace, keys.toArray(new String[0]));
    }

    @Override
    public void setAll(List<String> keys, List<Object> datas, int expiration, String keySpace) {
        Map<String, Serializable> dataMap = new HashMap<String, Serializable>();
        Iterator<String> kIter = keys.iterator();
        Iterator<Object> dIter = datas.iterator();
        while (kIter.hasNext()) {
            dataMap.put(kIter.next(), (Serializable) dIter.next());
        }

        if (!innerSitekvRedisMgr.existsKey(keySpace)) {

            innerSitekvRedisMgr.hmSet(keySpace, dataMap);
            if (!innerSitekvRedisMgr.extendTime(keySpace, (expiration << 10) & 0x7FFFFFFF)) {
                logger.error("set the expiration time of key {} to {} ,error ", keySpace, expiration);
            }
        } else {
            innerSitekvRedisMgr.hmSet(keySpace, dataMap);
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
     * @return 是否应该flush
     */
    private boolean shouldFlush(String key) {
        return !keyEviction.containsKey(key) || expiredTimeStamp > keyEviction.get(key);
    }

    @Override
    public boolean flush(String nameSpace, String key) {
        throw new UnsupportedOperationException("cant exec flush");
    }
}
