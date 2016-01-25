package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
@Component("__defaultFallBack")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FallBackProcessor extends Processor {

    private final static Logger logger = LoggerFactory.getLogger(FallBackProcessor.class);

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        FallbackProxy proxy = ctx.getFallBack();
        proxy.beforeProcess(ctx, this);
        try {
            return decoratee.doo(ctx, param);
        } catch (Throwable th) {
            logger.info("ctxId: {} error occurs, fall back, cause: ", ctx.getCtxId(), th);
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
