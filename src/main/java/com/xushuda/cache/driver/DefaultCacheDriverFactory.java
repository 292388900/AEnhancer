package com.xushuda.cache.driver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * factory class
 * 
 * @author xushuda
 *
 */
@Service
public class DefaultCacheDriverFactory implements CacheDriverFactory, ApplicationContextAware {

    private ApplicationContext context;

    private Map<Class<? extends CacheDriver>, CacheDriver> beanMap =
            new HashMap<Class<? extends CacheDriver>, CacheDriver>();

    @Override
    public CacheDriver getCacheDriver(Class<? extends CacheDriver> clazz) {
        CacheDriver bean = beanMap.get(clazz);
        if (null == bean) {
            bean = context.getBean(clazz);
            beanMap.put(clazz, bean);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
