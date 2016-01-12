package com.baidu.ascheduler.context;

import com.baidu.ascheduler.ext.Cacheable;
import com.baidu.ascheduler.ext.Splitable;

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

    public abstract Splitable getSpliter();

    public boolean cache();

    public abstract Cacheable getCacher();

    public boolean parallel();
}
