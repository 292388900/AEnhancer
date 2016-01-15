package com.baidu.aenhancer.core.processor;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.ext.ShortCircuitable;
import com.baidu.aenhancer.exception.CodingError;

public class DefaultShortCircuit implements ShortCircuitable {

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean shortcircuit(Method method, ShortCircuitTick tick) {
        // 默认使用全局设置
        return tick.shortcircuit(method);
    }

}
