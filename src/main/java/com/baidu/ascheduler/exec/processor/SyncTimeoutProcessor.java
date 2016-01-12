package com.baidu.ascheduler.exec.processor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.context.ShortCircuitType;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.ShortCircuitExcption;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.exec.DecoratableProcessor;
import com.baidu.ascheduler.exec.ProcessExecutor;

/**
 * 对外暴露的是同步的逻辑,可以为原有函数加上超时控制，
 * 
 * 需要运行的任务在别的线程，但是超时等待的逻辑在当前线程
 * 
 * @author xushuda
 *
 */
public class SyncTimeoutProcessor extends DecoratableProcessor {
    private final static Logger logger = LoggerFactory.getLogger(SyncTimeoutProcessor.class);

    @Override
    public Object process(ProcessContext ctx, Object param) throws Throwable {
        ProcessExecutor pool = ProcessExecutor.getInstance();
        Future<Object> data = pool.submitProcess(decoratee, ctx, param);
        int timeout = ctx.getTimeout();
        logger.info("ctxId: {} ,task start at {} while timeout is {}", ctx.getCtxId(), System.currentTimeMillis(),
                timeout);
        Object result = null;
        try {
            result = data.get(timeout, TimeUnit.MILLISECONDS);
            logger.info("ctxId: {} ,task finished at {}", ctx.getCtxId(), System.currentTimeMillis());
            return result;
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
            throw new ShortCircuitExcption("timeout exception", ShortCircuitType.TIMEOUT);
        }
    }

    @Override
    public void preCheck(final ProcessContext ctx, Object param) throws SchedAopException {
        if (decoratee == null) {
            throw new UnexpectedStateException("TimeoutProcessor is non-terminal processor, must have a decoratee");
        }
    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) throws SchedAopException {
        // TODO Auto-generated method stub

    }

}
