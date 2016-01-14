package com.baidu.aenhancer.core.processor;

import com.baidu.aenhancer.core.context.ProcessContext;

/**
 * 入口
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
                new ProcessorBuilder().isParallel(ctx.parallel()).isCache(ctx.cache()).isRetry(ctx.getRetry() > 0)
                        .isTimeout(ctx.getTimeout() > 0).isFallback(ctx.fallback()).isSplit(ctx.split());
        // build 处理器对象
        DecoratableProcessor processor = builder.build();
        // 处理
        return processor.doo(ctx, ctx.getArgs());
    }
}
