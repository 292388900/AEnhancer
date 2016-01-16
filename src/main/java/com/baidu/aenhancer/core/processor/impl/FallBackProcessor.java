package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;

public class FallBackProcessor extends Processor {
    public FallBackProcessor(Processor decoratee) {
        super(decoratee);
    }

    private final static Logger logger = LoggerFactory.getLogger(FallBackProcessor.class);

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        FallbackProxy proxy = ctx.getFallBack();
        proxy.beforeProcess(ctx, this);
        try {
            return decoratee.doo(ctx, param);
        } catch (Throwable th) {
            logger.info("error occurs, fall back, cause: ", th);
            return proxy.fallback((Object[]) param);
        }
    }

    @Override
    protected void preCheck(ProcessContext ctx, Object param) {

    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) {

    }

}
