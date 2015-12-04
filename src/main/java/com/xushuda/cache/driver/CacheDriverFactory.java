package com.xushuda.cache.driver;

public interface CacheDriverFactory {

    /**
     * 根据annotation中的driver的beanName获取
     * 
     * @param clazz
     * @return
     */
    public abstract CacheDriver getCacheDriver(String beanName);

}
