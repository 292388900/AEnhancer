package com.baidu.aenhancer.ext.impl.aggr;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.DecoratableProcessor;
import com.baidu.aenhancer.core.processor.ext.Cacheable;
import com.baidu.aenhancer.exception.CodingError;

public class CommonCacher implements Cacheable {

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
    }

    @Override
    public void beforeProcess(ProcessContext ctx, DecoratableProcessor currentProcess) {
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
