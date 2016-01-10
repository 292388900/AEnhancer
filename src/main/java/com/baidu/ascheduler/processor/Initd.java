package com.baidu.ascheduler.processor;

import com.baidu.ascheduler.context.ProcessContext;

/**
 * the processor 解析函数签名，注解等等
 * 
 * @author xushuda
 *
 */
public final class Initd {

    /**
     * 正常的处理流程 生成processor的规则，顺序在这里制定
     * 
     * @param ctx
     * @return
     * @throws Throwable
     */
    public Object start(ProcessContext ctx) throws Throwable {
        // 设置builder参数
        ProcessorBuilder builder =
                new ProcessorBuilder().setAggr(ctx.aggrInvok()).setBatch(ctx.getBatchSize() > 0)
                        .setCache(ctx.getCacheDriver() != null).setRetry(ctx.getRetry() > 0)
                        .setTimeout(ctx.getTimeout() > 0);
        ;
        // build 处理器对象
        DecoratableProcessor processor = builder.build();
        // 处理
        return processor.process(ctx, ctx.getArgs());
    }

    /**
     * 
     * @param ctx
     * @return
     * @throws Throwable
     */
    public Object startPlain(ProcessContext ctx) throws Throwable {
        DecoratableProcessor processor = new ProcessorBuilder().build();
        return processor.process(ctx, ctx.getArgs());
    }

    /**
     * 
     * @param ctx
     * @return
     * @throws Throwable
     */
    public Object startPlainWithBatch(ProcessContext ctx) throws Throwable {
        DecoratableProcessor processor = new ProcessorBuilder().setBatch(true).build();
        return processor.process(ctx, ctx.getArgs());
    }
}
