package com.baidu.aenhancer.ext.impl.aggr;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.Fallbackable;
import com.baidu.aenhancer.entry.FallbackMock;

public class ReturnNull implements Fallbackable {

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) {

    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {

    }

    @FallbackMock
    public Object fallback() {
        return null;
    }

}
