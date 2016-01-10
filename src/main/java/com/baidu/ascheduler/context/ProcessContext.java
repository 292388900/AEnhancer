package com.baidu.ascheduler.context;

import com.baidu.ascheduler.cache.driver.CacheDriver;
import com.baidu.ascheduler.exception.IllegalParamException;

public interface ProcessContext {

    public abstract long getCtxId();

    // 原函数相关－－－－－－－－－－－－－－－－
    public abstract Object invokeOrignialMethod(Object[] args) throws Throwable;

    public abstract Object[] getArgs();

    // 重试相关－－－－－－－－－－－－－
    public abstract int getRetry();

    // batch相关－－－－－－－－－－－－－－－－
    // PS：主要针对batch处理，不过batch相关的部分函数也需要在AggrCacheProcessor中使用
    public abstract Object[] replaceArgsWithKeys(Object keys);

    public abstract int getBatchSize();

    public abstract int getAggrPosition();

    public abstract boolean aggrInvok();

    public abstract Class<?> getRetType();

    public abstract Class<?> getAggParamType();

    // cache 相关－－－－－－－－－－－－－－－－－－
    public abstract Object getKeyFromResult(Object resultElement) throws IllegalParamException;

    public abstract Object getKeyFromParam(Object paramElement) throws IllegalParamException;

    public abstract int[] getIgnoreList();

    public abstract boolean relyOnSeqResult();

    public abstract CacheDriver getCacheDriver();

    public abstract String getNameSpace();

    public abstract int getExpiration();
    
    // 线程调度相关
    public abstract int getTimeout();

}
