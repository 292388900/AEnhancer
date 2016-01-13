package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.DecoratableProcessor;
import com.baidu.aenhancer.exception.UnexpectedStateException;

public class RetryProcessor extends DecoratableProcessor {
    private Logger logger = LoggerFactory.getLogger(RetryProcessor.class);

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        // 在最后一次重试前都catch所有异常
        for (int retryTimes = ctx.getRetry(); retryTimes > 1; --retryTimes) {
            try {
                return decoratee.doo(ctx, p);
            } catch (Throwable th) {
                logger.info("ctx_id: {} error , exception: {}, left ret times: {}", ctx.getCtxId(), th, retryTimes - 1);
            }
        }
        return decoratee.doo(ctx, p);
    }

    @Override
    public void preCheck(ProcessContext ctx, Object param) {
        if (null == decoratee) {
            throw new UnexpectedStateException("error decoratee, null for retry processor");
        }
        if (ctx.getRetry() < 0) {
            // should not reach here;
            throw new UnexpectedStateException("error retry times param");
        }
    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) {

    }

}
