package com.baidu.ascheduler.processor;

import java.rmi.UnexpectedException;

import com.baidu.ascheduler.model.ProcessContext;

/**
 * 普通的调用原函数
 * 
 * @author xushuda
 *
 */
public class PlainInvokProcessor implements DecoratableProcessor {

    private DecoratableProcessor decoratee;

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        if (decoratee != null) {
            throw new UnexpectedException("final processor can't decorate other processor");
        }
        // current start
        Object[] args = (Object[]) p;
        return ctx.invokeOrignialMethod(args);
        // current end
    }
}
