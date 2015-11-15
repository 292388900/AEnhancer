package com.xushuda.cache.driver;

public interface CacheDriverFactory {

    /**
     * 根据annotation中的clazz域获取driver
     * 
     * @param clazz
     * @return
     */
    public abstract CacheDriver getCacheDriver(Class<? extends CacheDriver> clazz);

}
