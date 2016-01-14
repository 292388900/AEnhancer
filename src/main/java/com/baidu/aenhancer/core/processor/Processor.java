package com.baidu.aenhancer.core.processor;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.exception.EnhancerRuntimeException;

/**
 * 
 * @author xushuda
 *
 */
public abstract class Processor {
    /**
     * 
     */
    protected Processor decoratee;

    public Processor(Processor decoratee) {
        this.decoratee = decoratee;
    }

    public final Object doo(ProcessContext ctx, Object param) throws Throwable {
        preCheck(ctx, param);
        Object ret = process(ctx, param);
        postCheck(ctx, ret);
        return ret;
    }

    /**
     * 真正的处理函数，处理函数
     * 
     * @param ctx
     * @param param
     * @return 返回值
     * @throws Throwable
     */
    protected abstract Object process(ProcessContext ctx, Object param) throws Throwable;

    /**
     * 让另一个processor hook到当前的processor后面
     * 
     * @param decoratee
     * @return
     */
    public final Processor hookee(Processor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    /**
     * 调用方法前验证上线文和参数
     * 
     * @param ctx
     * @param param
     * @throws EnhancerRuntimeException
     */
    protected abstract void preCheck(ProcessContext ctx, Object param);

    /**
     * 调用方法后，检查上下文和返回值
     * 
     * @param ctx
     * @param ret
     * @throws EnhancerRuntimeException
     */
    protected abstract void postCheck(ProcessContext ctx, Object ret);
}
