package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.ShortCircuitable;
import com.baidu.aenhancer.exception.CodingError;

public class DefaultShortCircuit implements ShortCircuitable {

    private Method method;

    // 记录数据
    private ShortCircuitTick sct;

    // 状态机
    private ShortCircuitStateMachine scsm;

    private int calSize = 2;

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
        method = ((MethodSignature) jp.getSignature()).getMethod();
        sct = ShortCircuitTick.getInstance();
        scsm = ShortCircuitStateMachine.getInstance();
    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean shortcircuit() {
        return scsm.shortcircuit(method, sct.getSuccesss(method, calSize), sct.getTimeouts(method, calSize),
                sct.getErrors(method, calSize), sct.getRejections(method, calSize), sct.getTick());
    }

    @Override
    public void reject() {
        sct.reject(method);
        scsm.notify(method, false, sct.getTick());
    }

    @Override
    public void timeout() {
        sct.timeout(method);
        scsm.notify(method, false, sct.getTick());

    }

    @Override
    public void error() {
        sct.error(method);
        scsm.notify(method, false, sct.getTick());
    }

    @Override
    public void success() {
        sct.success(method);
        scsm.notify(method, true, sct.getTick());
    }

}
