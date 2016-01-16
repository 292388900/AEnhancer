package com.baidu.aenhancer.core.processor.ext.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.HookProxy;
import com.baidu.aenhancer.exception.CodingError;

/**
 * 默认的Hook入口
 * 
 * @author xushuda
 *
 */
public final class DefaultHook implements HookProxy {
    // ctx
    private ProcessContext ctx;
    private ApplicationContext context;

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
        this.context = context;
    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
        this.ctx = ctx;
    }

    /**
     * 正常的处理流程 生成processor的规则，顺序在这里制定
     * 
     * @param ctx
     * @return
     * @throws Throwable
     */
    @Override
    public Object call(Object[] param) throws Throwable {
        // 设置builder参数
        ProcessorBuilder builder = new ProcessorBuilder()//
                .isParallel(ctx.parallel())//
                .isCache(ctx.cache())//
                .isRetry(ctx.getRetry() > 0)//
                .isTimeout(ctx.getTimeout() > 0)//
                .isFallback(ctx.fallback())//
                .isSplit(ctx.split())//
                .isShortcircuit(ctx.shortcircuit());
        // build 处理器对象
        Processor processor = builder.build();
        // 处理
        return processor.doo(ctx, ctx.getArgs());
    }
}
