package com.baidu.ascheduler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.cache.driver.CacheDriver;
import com.baidu.ascheduler.model.ProcessContext;

public class PlainCacheProcessor extends AbsCacheProcessor {

    private Logger logger = LoggerFactory.getLogger(PlainCacheProcessor.class);
    private DecoratableProcessor decoratee;

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        // TODO Auto-generated method stub
        Object[] args = (Object[]) p; // a new copy
        CacheDriver driver = ctx.getCacheDriver();
        // CacheDriver driver = fac.getCacheDriver(ctx.getDriver());
        Object data = driver.get(getKey(args, ctx), ctx.getNameSpace());
        logger.info("data retrieved for key {} is {}", args, data);
        if (null == data) {
            logger.info("data doesn't exists in cache, start to call the next process with args {}", args);
            // 用原参数直接调用下一个process
            data = decoratee.process(ctx, p);
            if (null != data) {
                driver.set(getKey(args, ctx), data, ctx.getExpiration(), ctx.getNameSpace());
                logger.info("get data: {}, and saved to cache", data);
            } else {
                logger.warn("the data got from target procedure is still null");
            }
        }
        return data;
    }

}
