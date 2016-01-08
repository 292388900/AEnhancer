package com.baidu.ascheduler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.ShortCircuitExcption;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.model.Aggregation;
import com.baidu.ascheduler.model.ProcessContext;

public class BatchProcessor implements DecoratableProcessor {

    private Logger logger = LoggerFactory.getLogger(BatchProcessor.class);

    private DecoratableProcessor decoratee;

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    /**
     * 分多次调用下一个processor
     * 
     * @param Object p
     * @throws Throwable
     */
    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        validateCtx(ctx, p);
        Aggregation param = new Aggregation(ctx.getAggParamType(), (Object[].class.cast(p))[ctx.getAggrPosition()]);
        if (param.isEmpty()) {

        }
        // 将多次调用的值都放入unCachedResult中
        Aggregation result = new Aggregation(ctx.getRetType());
        // the argument，如果必要（batch）会分为多批
        for (Aggregation splited : param.split(ctx.getBatchSize())) {
            // get the data from target process
            logger.info("ctx_id: {} unCached keys exist, call the target process to get data, keys args are : {}",
                    ctx.getCtxId(), splited);
            // 替换后的参数 设置为结果，作为下一个processor的参数
            Object nxtParam = ctx.replaceArgsWithKeys(splited.toInstance());
            Object rawResult = decoratee.process(ctx, nxtParam);
            // 从下一个processor获取结果
            if (null != rawResult) {
                Aggregation deltaResult = new Aggregation(ctx.getRetType(), rawResult);
                logger.info("ctx_id: {} data is get from target process : {}", ctx.getCtxId(), deltaResult);
                result.addAll(deltaResult);
            } else {
                logger.warn("ctx_id: {} the data is not avaliable from the procedure", ctx.getCtxId());
            }
        }
        return result;

    }

    @Override
    public void validateCtx(ProcessContext ctx, Object param) throws SchedAopException {
        if (decoratee == null) {
            throw new UnexpectedStateException("decoratee can't be null for Batch Processor");
        }
        // batch 首先肯定是aggr
        if (!ctx.aggrInvok()) {
            throw new UnexpectedStateException("not aggrInvk for Batch  Processor");
        }
        if (!(param instanceof Object[])) {
            throw new UnexpectedStateException("param must be Object[] for Batch  Processor");
        }
        Object[] p = Object[].class.cast(param);
        if (p.length < ctx.getAggrPosition()) {
            // should not reach here
            throw new UnexpectedStateException("error param length");
        }

        if (null == p[ctx.getAggrPosition()]) {
            throw new ShortCircuitExcption("null aggr param");
        }
    }

}
