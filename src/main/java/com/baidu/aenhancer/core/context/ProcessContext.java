package com.baidu.aenhancer.core.context;

import com.baidu.aenhancer.core.processor.ext.Cacheable;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
import com.baidu.aenhancer.core.processor.ext.SplitProxy;

public interface ProcessContext {

    public abstract long getCtxId();

    // 原函数相关－－－－－－－－－－－－－－－－
    public abstract Object invokeOrignialMethod(Object[] args) throws Throwable;

    public abstract Object[] getArgs();

    // 重试相关－－－－－－－－－－－－－
    public abstract int getRetry();

    // 线程调度相关
    public abstract int getTimeout();

    public boolean split();

    public abstract SplitProxy getSpliter();

    public boolean cache();

    public abstract Cacheable getCacher();

    public boolean parallel();

    public boolean fallback();

    public abstract FallbackProxy getFallBack();

    public String getGroup();
}
