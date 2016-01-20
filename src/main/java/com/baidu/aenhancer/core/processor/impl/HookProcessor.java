package com.baidu.aenhancer.core.processor.impl;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HookProcessor extends Processor {

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        return decoratee.doo(ctx, param);
    }

    @Override
    protected void preCheck(ProcessContext ctx, Object param) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) {
        // TODO Auto-generated method stub

    }

}
