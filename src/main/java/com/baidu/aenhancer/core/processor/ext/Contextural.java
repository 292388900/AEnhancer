package com.baidu.aenhancer.core.processor.ext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.exception.CodingError;
import com.baidu.aenhancer.exception.IllegalParamException;

/**
 * 
 * @author xushuda
 *
 */
public interface Contextural {

    /**
     * 在进入切面的时候被调用
     * 
     * @param jp
     * @param context
     * @throws CodingError 
     * @throws IllegalParamException
     */
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError;

    /**
     * 在执行当前Process的时候被调用
     * 
     * @param ctx
     * @param currentProcess
     */
    public void beforeProcess(ProcessContext ctx, Processor currentProcess);
}
