package com.baidu.ascheduler.exec.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.exec.DecoratableProcessor;

/**
 * 普通的调用原函数
 * 
 * @author xushuda 
 *
 */
public class PlainInvokProcessor extends DecoratableProcessor {
    private Logger logger = LoggerFactory.getLogger(PlainInvokProcessor.class);

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        // current start
        logger.info("invok the actual method");
        Object[] args = (Object[]) p;
        return ctx.invokeOrignialMethod(args);
        // current end
    }

    @Override
    public void preCheck(ProcessContext ctx, Object param) throws SchedAopException {
        if (decoratee != null) {
            throw new UnexpectedStateException("final processor can't decorate other processor");
        }
        if (!(param instanceof Object[])) {
            throw new UnexpectedStateException("error param for Plain Invoke Processor");
        }
    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) throws SchedAopException {
        // TODO Auto-generated method stub
        
    }
}
