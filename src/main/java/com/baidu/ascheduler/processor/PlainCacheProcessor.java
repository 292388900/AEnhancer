package com.baidu.ascheduler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.cache.driver.CacheDriver;
import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;

public class PlainCacheProcessor extends AbsCacheProcessor {

    private final static Logger logger = LoggerFactory.getLogger(PlainCacheProcessor.class);
    private DecoratableProcessor decoratee;

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        validateCtx(ctx, p);
        Object[] args = (Object[]) p; // a new copy
        CacheDriver driver = ctx.getCacheDriver();
        // CacheDriver driver = fac.getCacheDriver(ctx.getDriver());
        Object data = driver.get(getKey(args, ctx), ctx.getNameSpace());
        logger.info("ctx_id: {} data retrieved for key {} is {}", ctx.getCtxId(), args, data);
        if (null == data) {
            logger.info("ctx_id: {} data doesn't exists in cache, start to call the next process with args {}",
                    ctx.getCtxId(), args);
            // 用原参数直接调用下一个process
            data = decoratee.process(ctx, p);
            if (null != data) {
                driver.set(getKey(args, ctx), data, ctx.getExpiration(), ctx.getNameSpace());
                logger.info("ctx_id: {} get data: {}, and saved to cache", ctx.getCtxId(), data);
            } else {
                logger.warn("ctx_id: {} the data got from target procedure is still null", ctx.getCtxId());
            }
        }
        return data;
    }

    @Override
    public void validateCtx(ProcessContext ctx, Object param) throws SchedAopException {
        if (null == ctx.getCacheDriver()) {
            throw new UnexpectedStateException("no cache driver available");
        }
    }

}
