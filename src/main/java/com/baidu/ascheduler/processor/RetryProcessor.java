package com.baidu.ascheduler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.model.ProcessContext;

public class RetryProcessor implements DecoratableProcessor {
    private Logger logger = LoggerFactory.getLogger(RetryProcessor.class);

    private DecoratableProcessor decoratee;

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        validateCtx(ctx, p);
        // 在最后一次重试前都catch所有异常
        for (int retryTimes = ctx.getRetry(); retryTimes > 1; --retryTimes) {
            try {
                return decoratee.process(ctx, p);
            } catch (Throwable th) {
                logger.info("ctx_id: {} error , exception: {}, left ret times: {}",ctx.getCtxId(), th, retryTimes - 1);
            }
        }
        return decoratee.process(ctx, p);
    }

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {

        this.decoratee = decoratee;
        return decoratee;
    }

    @Override
    public void validateCtx(ProcessContext ctx, Object param) throws SchedAopException {
        if (null == decoratee) {
            throw new UnexpectedStateException("error decoratee, null for retry processor");
        }
        if (ctx.getRetry() < 0) {
            // should not reach here;
            throw new UnexpectedStateException("error retry times param");
        }
    }

}
