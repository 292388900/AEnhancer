package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.exception.UnexpectedStateException;

/**
 * 普通的调用原函数
 * 
 * @author xushuda
 *
 */
@Component
@Scope("prototype")
public class PlainInvokProcessor extends Processor {
    public PlainInvokProcessor(){
        super(null);
    }
    public PlainInvokProcessor(Processor decoratee) {
        super(decoratee);
    }

    private Logger logger = LoggerFactory.getLogger(PlainInvokProcessor.class);

    @Override
    protected Object process(ProcessContext ctx, Object p) throws Throwable {
        // current start
        logger.info("ctxId: {} invok the actual method", ctx.getCtxId());
        Object[] args = (Object[]) p;
        return ctx.invokeOrignialMethod(args);
        // current end
    }

    @Override
    protected void preCheck(ProcessContext ctx, Object param) {
        if (decoratee != null) {
            throw new UnexpectedStateException("final processor can't decorate other processor");
        }
        if (!(param instanceof Object[])) {
            throw new UnexpectedStateException("error param for Plain Invoke Processor");
        }
    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) {

    }
}
