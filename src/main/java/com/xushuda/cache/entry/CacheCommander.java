package com.xushuda.cache.entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xushuda.cache.RedisHa;

/**
 * TODO 后续支持单独刷不同接口的缓存
 * 
 * @author xushuda
 *
 */
@Service
public class CacheCommander {

    @Autowired
    private RedisHa redis;

    public void flushAll() {
        redis.flushAll();
    }
}
