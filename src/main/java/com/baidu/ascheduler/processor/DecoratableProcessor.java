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

    public DecoratableProcessor decorate(DecoratableProcessor decoratee);

    public abstract void validateCtx(ProcessContext ctx, Object param) throws SchedAopException;

}
