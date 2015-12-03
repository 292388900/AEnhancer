package com.xushuda.cache.entry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import com.xushuda.cache.processor.CacheFrontProcessor;

/**
 * 定义不同的pointcut
 * 
 * @author xushuda
 *
 */
@Aspect
public class CachePointCut {

    @Autowired
    private CacheFrontProcessor aopProcessor;

    @Around("@annotation(cached)")
    public Object around(ProceedingJoinPoint jp, Cached cached) throws Throwable {
        return aopProcessor.aopAround(jp, cached);
    }
}
