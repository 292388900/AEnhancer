package com.baidu.ascheduler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;

/**
 * 普通的调用原函数
 * 
 * @author xushuda
 *
 */
public class PlainInvokProcessor implements DecoratableProcessor {
    private Logger logger = LoggerFactory.getLogger(PlainInvokProcessor.class);

    private DecoratableProcessor decoratee;

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        validateCtx(ctx, p);
        // current start
        logger.info("invok the actual method");
        Object[] args = (Object[]) p;
        return ctx.invokeOrignialMethod(args);
        // current end
    }

    @Override
    public void validateCtx(ProcessContext ctx, Object param) throws SchedAopException {
        if (decoratee != null) {
            throw new UnexpectedStateException("final processor can't decorate other processor");
        }
        if (!(param instanceof Object[])) {
            throw new UnexpectedStateException("error param for Plain Invoke Processor");
        }
    }
}
