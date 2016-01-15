package com.baidu.aenhancer.core.processor.impl;

import java.lang.reflect.Method;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ShortCircuitTick;
import com.baidu.aenhancer.exception.EnhancerRuntimeException;
import com.baidu.aenhancer.exception.ShortCircuitExcption;

public class ShortCircuitProcessor extends Processor {

    public ShortCircuitProcessor(Processor decoratee) {
        super(decoratee);
    }

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        ShortCircuitTick sct = ShortCircuitTick.getInstanc();
        Method method = ctx.getMothod();
        try {
            if (!sct.shortcircuit(method)) {
                Object ret = decoratee.doo(ctx, param);
                sct.success(method);
                return ret;
            }
        } catch (ShortCircuitExcption e) {
            switch (e.getShortCircuitType()) {
                case TASK_REJ:
                    sct.reject(method);
                    break;
                case TIMEOUT:
                    sct.timeout(method);
                    break;
                case FAILURE:
                    sct.error(method);
                    break;
                default:
                    sct.error(method);
            }
            throw new EnhancerRuntimeException(e.getCause());
        } catch (Throwable e) {
            sct.error(method);
            throw e;
        }
        // 如果只是短路,直接抛出RunTime异常
        throw new EnhancerRuntimeException("ctxId: " + ctx.getCtxId() + ", the processor is short circuit");
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
