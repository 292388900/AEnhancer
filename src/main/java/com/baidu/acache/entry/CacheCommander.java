package com.baidu.acache.entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baidu.acache.driver.DefaultCacheDriverFactory;

/**
 * TODO 后续支持单独刷不同接口的缓存
 * 
 * @author xushuda
 *
 */
@Service
public class CacheCommander {

    @Autowired
    private DefaultCacheDriverFactory fac;

    public void flushAll(String beanName) {
        fac.getCacheDriver(beanName).flushAll();
    }
}
