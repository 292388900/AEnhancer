package com.baidu.ascheduler.processor;

import com.baidu.ascheduler.model.ProcessContext;

public interface DecoratableProcessor {

    /**
     * 处理函数
     * 
     * @param ctx
     * @param param
     * @return 返回值
     * @throws Throwable
     */
    public Object process(ProcessContext ctx, Object param) throws Throwable;

    /**
     * 
     * @param decoratee 被装饰对象
     * @return DecoratableProcessor 返回自身对象
     */
    public DecoratableProcessor decorate(DecoratableProcessor decoratee);
}
