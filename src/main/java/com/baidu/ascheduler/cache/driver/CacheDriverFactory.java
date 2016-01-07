package com.baidu.ascheduler.cache.driver;

public interface CacheDriverFactory {

    /**
     * 根据annotation中的driver的beanName获取
     * 
     * @param clazz
     * @return CacheDriver 对象
     */
    public abstract CacheDriver getCacheDriver(String beanName);

}
