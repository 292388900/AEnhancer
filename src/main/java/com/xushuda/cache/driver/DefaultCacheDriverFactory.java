package com.xushuda.cache.driver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * factory class , current simply return clazz.newInstance
 * 
 * @author xushuda
 *
 */
@Service
// TODO 不作为自动加载，使用application context 配置
public class DefaultCacheDriverFactory implements CacheDriverFactory{

    @Autowired
    private CacheDriver redisHa;

    @Override
    public CacheDriver getCacheDriver(Class<? extends CacheDriver> clazz) {
        return redisHa;
    }
}
