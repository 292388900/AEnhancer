package com.baidu.ascheduler.processor;

import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.model.ProcessContext;

public interface DecoratableProcessor {

    /**
     * 真正的处理函数，处理函数
     * 
     * @param ctx
     * @param param
     * @return 返回值
     * @throws Throwable
     */
    public abstract Object process(ProcessContext ctx, Object param) throws Throwable;

    /**
     * 装饰另一个processor
     * 
     * @param decoratee
     * @return
     */
    public DecoratableProcessor decorate(DecoratableProcessor decoratee);

    /**
     * 验证上线文的正确性
     * 
     * @param ctx
     * @param param
     * @throws SchedAopException
     */
    public abstract void validateCtx(ProcessContext ctx, Object param) throws SchedAopException;

}
