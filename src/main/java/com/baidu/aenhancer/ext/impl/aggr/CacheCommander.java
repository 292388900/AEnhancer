package com.baidu.aenhancer.ext.impl.aggr;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * TODO 后续支持单独刷不同接口的缓存
 * 
 * @author xushuda
 *
 */
public class CacheCommander implements ApplicationContextAware {

    private ApplicationContext context;

    public void flushAll(String beanName) {
        // 会抛出notsuchbean等异常
        context.getBean(beanName, CacheDriver.class).flushAll();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
