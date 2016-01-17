package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.ShortCircuitable;
import com.baidu.aenhancer.exception.EnhancerRuntimeException;
import com.baidu.aenhancer.exception.ShortCircuitExcption;

public class ShortCircuitProcessor extends Processor {
    private Logger logger = LoggerFactory.getLogger(ShortCircuitProcessor.class);

    public ShortCircuitProcessor(Processor decoratee) {
        super(decoratee);
    }

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        ShortCircuitable sct = ctx.getShortCircuit();
        sct.beforeProcess(ctx, this);
        try {
            // 不在此短路的话则调用
            if (!sct.shortcircuit()) {
                Object ret = decoratee.doo(ctx, param);
                sct.success();
                return ret;
            }
        } catch (ShortCircuitExcption e) {
            logger.info("ctxId {} ,ShortCircuitException occurs , type: {}", ctx.getCtxId(), e.getShortCircuitType());
            switch (e.getShortCircuitType()) {
                case TASK_REJ:
                    sct.reject();
                    break;
                case TIMEOUT:
                    sct.timeout();
                    break;
                case FAILURE:
                    sct.error();
                    break;
                default:
                    sct.error();
            }
            throw new EnhancerRuntimeException(e.getCause());
        } catch (Throwable e) {
            logger.info("ctxId {} ,error occurs,short circuit processor mark error() cause: ", ctx.getCtxId(), e);
            sct.error(); // mark error
            throw e;
        }
        logger.info("ctxId {} is shortcircuited ", ctx.getCtxId());
        // 如果是短路状态，直接抛出运行时异常
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
