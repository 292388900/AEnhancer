package com.baidu.aenhancer.core.context;

import java.lang.reflect.Method;

import com.baidu.aenhancer.core.processor.ext.CacheProxy;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
import com.baidu.aenhancer.core.processor.ext.HookProxy;
import com.baidu.aenhancer.core.processor.ext.ShortCircuitable;
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

    public abstract CacheProxy getCacher();

    public boolean parallel();

    public boolean fallback();

    public abstract FallbackProxy getFallBack();

    public String getGroup();

    public abstract boolean hook();

    public abstract HookProxy getHook();

    public Method getMothod();

    public abstract boolean shortcircuit();

    public abstract ShortCircuitable getShortCircuit();
}
