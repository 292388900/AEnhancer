package com.baidu.ascheduler.processor;

import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.model.ProcessContext;

/**
 * 
 * @author xushuda
 *
 */
public class ScheduledInvokProcessor implements DecoratableProcessor {

    private DecoratableProcessor decoratee;

    @Override
    public Object process(ProcessContext ctx, Object param) throws Throwable {
        validateCtx(ctx, param);
        return null;
    }

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    @Override
    public void validateCtx(ProcessContext ctx, Object param) throws SchedAopException {
        // TODO Auto-generated method stub
        if (decoratee != null) {
            throw new UnexpectedStateException("final processor can't decorate other processor");
        }
    }

}
