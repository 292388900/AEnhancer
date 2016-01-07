package com.baidu.ascheduler.cache.driver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 默认的 factory class 使用spring注入需要的driver 类
 * 
 * @author xushuda
 *
 */
public final class DefaultCacheDriverFactory implements CacheDriverFactory, ApplicationContextAware {

    private ApplicationContext context;

    private Map<String, CacheDriver> beanMap = new ConcurrentHashMap<String, CacheDriver>();

    @Override
    public CacheDriver getCacheDriver(String beanName) {
        CacheDriver bean = beanMap.get(beanName);
        if (null == bean) {
            // 从当前的spring 容器获取对应的bean，如果一个clazz对应了多个bean,或者没有相应的bean，则会抛出异常
            bean = context.getBean(beanName, CacheDriver.class);
            beanMap.put(beanName, bean);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
