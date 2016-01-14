package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.HookProxy;

/**
 * 所有Processor的入口，但是它并不会引用别的Processor
 * 
 * @author xushuda
 *
 */
public class EverythingStartFromHere extends Processor {

    public EverythingStartFromHere(Processor decoratee) {
        super(decoratee);
    }

    private final static Logger logger = LoggerFactory.getLogger(EverythingStartFromHere.class);

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        HookProxy hook = ctx.getHook();
        hook.beforeProcess(ctx, this);
        logger.info("hook processor will be called \"{}\"", hook);
        return hook.call((Object[]) param);
    }

    @Override
    protected void preCheck(ProcessContext ctx, Object param) {
        logger.info("ctxId: {}, precheck, hook processor's hooker is: \"{}\"", ctx.getCtxId(), ctx.getHook());
    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) {
        logger.info("ctxId: {}, postcheck hook processor ends, ret is: \"{}\"", ctx.getCtxId(), ret);
    }

}
