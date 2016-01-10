package com.baidu.ascheduler.processor;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exec.ProcessExecutor;

/**
 * 提供并行能力
 * 
 * TODO
 * 
 * @author xushuda
 *
 */
public abstract class AsyncParallelProcessor implements DecoratableProcessor {

    private final static Logger logger = LoggerFactory.getLogger(AsyncParallelProcessor.class);

    @Override
    public Future<Object> process(final ProcessContext ctx, final Object param) throws Throwable {
        ProcessExecutor pool = ProcessExecutor.getInstance();
        Future<Object> ret = pool.scheduled(decoratee, ctx, param);
        
        return ret;
    }

    private DecoratableProcessor decoratee;

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    @Override
    public void validateCtx(ProcessContext ctx, Object param) throws SchedAopException {

    }

}
