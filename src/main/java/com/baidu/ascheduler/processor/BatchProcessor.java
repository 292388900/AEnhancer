package com.baidu.ascheduler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.exception.IllegalParamException;
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
     * @throws Throwable
     */
    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        Aggregation param = getParam(p);
        if (param.isEmpty()) {

        }
        // 将多次调用的值都放入unCachedResult中
        Aggregation result = new Aggregation(ctx.getRetType());
        // the argument，如果必要（batch）会分为多批
        for (Aggregation splited : param.split(ctx.getBatchSize())) {
            // get the data from target process
            logger.info("unCached keys exist, call the target process to get data, keys args are : {}", splited);
            // splited 设置为结果，作为下一个processor的参数
            Object nxtParam = ctx.replaceArgsWithKeys(splited.toInstance());
            Object rawResult = decoratee.process(ctx, nxtParam);
            // 从下一个processor获取结果
            Aggregation deltaResult = (Aggregation) rawResult;
            if (null != deltaResult) {
                logger.info("data is get from target process : {}", deltaResult);
                result.addAll(deltaResult);
            } else {
                logger.warn("the data is not avaliable from the procedure");
            }
        }
        return result;

    }

    private Aggregation getParam(Object p) throws IllegalParamException {
        if (p == null) {
            throw new IllegalParamException("null param for batch invok processor");
        }
        if (!(p instanceof Aggregation)) {
            throw new IllegalParamException("non aggregation param for batch invok processor");
        }
        return Aggregation.class.cast(p);
    }

}
