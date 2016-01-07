package com.baidu.ascheduler.processor;

import java.rmi.UnexpectedException;

import com.baidu.ascheduler.model.ProcessContext;

public class ScheduledInvokProcessor implements DecoratableProcessor {

    private DecoratableProcessor decoratee;

    @Override
    public Object process(ProcessContext ctx, Object param) throws Throwable {
        if (decoratee != null) {
            throw new UnexpectedException("final processor can't decorate other processor");
        }
        return null;
    }

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

}
