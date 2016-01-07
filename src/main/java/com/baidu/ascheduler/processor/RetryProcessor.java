package com.baidu.ascheduler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.model.ProcessContext;

public class RetryProcessor implements DecoratableProcessor {
    private Logger logger = LoggerFactory.getLogger(RetryProcessor.class);

    private DecoratableProcessor processor;

    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        // 在最后一次重试前都catch所有异常
        for (int retryTimes = ctx.getRetry(); retryTimes > 1; --retryTimes) {
            try {
                return processor.process(ctx, p);
            } catch (Throwable th) {
                logger.info("error , exception: {}, left ret times: {}", th, retryTimes - 1);
            }
        }
        return processor.process(ctx, p);
    }

    @Override
    public DecoratableProcessor decorate(DecoratableProcessor decoratee) {

        return null;
    }

}
