package com.baidu.ascheduler.ext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.IllegalParamException;
import com.baidu.ascheduler.exec.DecoratableProcessor;

public interface Contextural {

    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws IllegalParamException;

    public void beforeProcess(ProcessContext ctx, DecoratableProcessor currentProcess);
}
