package com.baidu.ascheduler.processor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.exec.ProcessExecutor;

/**
 * 对外暴露的是同步的逻辑,可以为原有函数加上超时控制
 * 
 * @author xushuda
 *
 */
public class SyncTimeoutProcessor implements DecoratableProcessor {
    private final static Logger logger = LoggerFactory.getLogger(SyncTimeoutProcessor.class);

    private DecoratableProcessor decoratee;

    @Override
    public Object process(ProcessContext ctx, Object param) throws Throwable {
        validateCtx(ctx, param);
        ProcessExecutor pool = ProcessExecutor.getInstance();
        Future<Object> data = pool.scheduled(decoratee, ctx, param);
        int timeout = ctx.getTimeout();
        logger.info("ctxId: {} ,Timeout processor get Future at {} while timeout is {}", ctx.getCtxId(),
                System.currentTimeMillis(), timeout);
        Object result = null;
        try {
            result = data.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            logger.error("ctxId: {} ,error thrown by future, task error caused by: {}", ctx.getCtxId(), e);
            data.cancel(true);
            // 直接抛出原因
            throw e.getCause();
        } catch (InterruptedException e) {
            logger.error("ctxId: {} ,error thrown by future, task interrupted caused by: {}", ctx.getCtxId(), e);
            // 这种状况向上层抛出UnexpectedStateException
            data.cancel(true);
            throw new UnexpectedStateException(e);
        } catch (TimeoutException e) {
            logger.info("ctxId: {} ,task timeout at {}", ctx.getCtxId(), System.currentTimeMillis());
            data.cancel(true);
            // TODO add FallBackHandler，现在直接返回null
            return null;
        }
        logger.info("ctxId: {} ,task finished at {}", ctx.getCtxId(), System.currentTimeMillis());
        return result;
    }

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    @Override
    public void validateCtx(final ProcessContext ctx, Object param) throws SchedAopException {
        if (decoratee == null) {
            throw new UnexpectedStateException("TimeoutProcessor is non-terminal processor, must have a decoratee");
        }
    }

}
