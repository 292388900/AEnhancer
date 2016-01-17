package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.HookProxy;
import com.baidu.aenhancer.exception.EnhancerRuntimeException;

/**
 * 所有Processor的入口，但是它并不会引用别的Processor，而是引用了Hook
 * 
 * @author xushuda
 *
 */
public class InceptProcessor extends Processor {

    public InceptProcessor(Processor decoratee) {
        super(decoratee);
    }

    private final static Logger logger = LoggerFactory.getLogger(InceptProcessor.class);

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        HookProxy hook = ctx.getHook();
        hook.beforeProcess(ctx, this);
        logger.info("hook processor will be called \"{}\"", hook);
        return hook.call((Object[]) param);
    }

    @Override
    protected void preCheck(ProcessContext ctx, Object param) {
        if (null != decoratee) {
            throw new EnhancerRuntimeException("this processor only call hook, no decoratee should be present");
        }
        logger.info("ctxId: {}, precheck, hook processor's hooker is: \"{}\"", ctx.getCtxId(), ctx.getHook());
    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) {
        logger.info("ctxId: {}, postcheck hook processor ends, ret is: \"{}\"", ctx.getCtxId(), ret);
    }

}
