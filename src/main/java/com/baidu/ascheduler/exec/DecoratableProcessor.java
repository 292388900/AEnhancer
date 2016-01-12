package com.baidu.ascheduler.exec;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;

/**
 * 
 * @author xushuda
 *
 * @param <RET> 返回的范型
 * @param <PARAM> 参数的范型
 */
public abstract class DecoratableProcessor {
    /**
     * 
     */
    protected DecoratableProcessor decoratee;

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
     * 装饰另一个processor
     * 
     * @param decoratee
     * @return
     */
    public final DecoratableProcessor decorate(DecoratableProcessor decoratee) {
        this.decoratee = decoratee;
        return this;
    }

    /**
     * 调用方法前验证上线文和参数
     * 
     * @param ctx
     * @param param
     * @throws SchedAopException
     */
    protected abstract void preCheck(ProcessContext ctx, Object param) throws SchedAopException;

    /**
     * 调用方法后，检查上下文和返回值
     * 
     * @param ctx
     * @param ret
     * @throws SchedAopException
     */
    protected abstract void postCheck(ProcessContext ctx, Object ret) throws SchedAopException;
}
