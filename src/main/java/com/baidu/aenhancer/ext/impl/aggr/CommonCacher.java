package com.baidu.aenhancer.ext.impl.aggr;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.CacheProxy;
import com.baidu.aenhancer.exception.CodingError;

public class CommonCacher implements CacheProxy {

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
    }

    @Override
    public Object retrieveFromCache(Object[] param) {
        return param;
    }

    @Override
    public boolean allCached() {
        return false;
    }

    @Override
    public Object cacheNcollapse(Object resultFromNxtProc) {
        return resultFromNxtProc;
    }

}
