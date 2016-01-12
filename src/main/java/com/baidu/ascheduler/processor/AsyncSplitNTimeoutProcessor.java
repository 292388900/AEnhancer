package com.baidu.ascheduler.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.context.ShortCircuitType;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.ShortCircuitExcption;
import com.baidu.ascheduler.exec.DecoratableProcessor;
import com.baidu.ascheduler.exec.ProcessExecutor;
import com.baidu.ascheduler.ext.Splitable;

/**
 * 提供并行能力,和timeout控制,这里没有办法，timeout控制和并行是藕合的
 * 
 * TODO
 * 
 * @author xushuda
 *
 */
public class AsyncSplitNTimeoutProcessor extends DecoratableProcessor {

    private final static Logger logger = LoggerFactory.getLogger(AsyncSplitNTimeoutProcessor.class);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object process(final ProcessContext ctx, final Object p) throws Throwable {
        Splitable spliter = ctx.getSpliter();
        // ctx
        spliter.beforeProcess(ctx, this);
        // split 参数
        List params = spliter.split((Object[]) p);
        // 结果集合
        List<Object> results = new ArrayList<Object>();
        try {
            logger.info("ctxId: {}, {} jobs committed at {} with timeout(0 for immortals): {}", ctx.getCtxId(),
                    params.size(), System.currentTimeMillis(), ctx.getTimeout());
            // 在这个方法这里可能会阻塞timeout 秒时长
            List<Future<Object>> futures =
                    ctx.getTimeout() > 0 ? ProcessExecutor.getInstance().submitProcess(decoratee, ctx, params,
                            ctx.getTimeout()) : ProcessExecutor.getInstance().submitProcess(decoratee, ctx, params);
            // 如果没有被rej，interrupt，则都完成
            for (Future<Object> future : futures) {
                // 如果取消（超时未完成会变为取消）
                if (future.isCancelled()) {
                    throw new ShortCircuitExcption("splited call timeout", ShortCircuitType.TIMEOUT);
                }
                results.add(future.get());
            }
            // collapse
            logger.info("ctxId: {}, get all splited task completed successfully at time:{} ", ctx.getCtxId(),
                    System.currentTimeMillis());
            return spliter.collapse(results);
        } catch (InterruptedException e) {
            logger.info("ctxId: {} ,interrupted while waiting for splited task to complete at time:{} cause: {}",
                    ctx.getCtxId(), System.currentTimeMillis(), e);
            throw e; // throw
        } catch (ExecutionException e) {
            logger.error("ctxId: {} ,error thrown by future,splited task error at time:{}  caused by: {}",
                    ctx.getCtxId(), System.currentTimeMillis(), e);
            throw e;
        } catch (RejectedExecutionException e) {
            logger.error("ctxId: {} , executor reject new task at time:{}  caused by: {}", ctx.getCtxId(),
                    System.currentTimeMillis(), e);
            throw new ShortCircuitExcption("task rejcted", ShortCircuitType.TASK_REJ);
        }

    }

    @Override
    public void preCheck(ProcessContext ctx, Object param) throws SchedAopException {

    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) throws SchedAopException {

    }

}
