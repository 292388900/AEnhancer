package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.context.ShortCircuitType;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.ShortCircuitProxy;
import com.baidu.aenhancer.exception.CodingError;

@Component("circuit")
@Scope("prototype")
public class DefaultShortCircuit implements ShortCircuitProxy {

    private Method method;

    // 记录数据
    private ShortCircuitTick sct;

    // 状态机
    private ShortCircuitStateMachine scsm;

    // 计算窗口的大小
    private int calSize;

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
        // 这个signature是父类的signature
        method = ((MethodSignature) jp.getSignature()).getMethod();
        sct = ShortCircuitTickFactory.getTick();
        scsm = ShortCircuitStateMachineFactory.getStateMachine(method, sct.getTick());
        calSize = scsm.getAggregationSize();
    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
    }

    @Override
    public boolean shortcircuit() {
        return scsm.shortcircuit(sct.getSuccesss(method, calSize), sct.getTimeouts(method, calSize),
                sct.getErrors(method, calSize), sct.getRejections(method, calSize), sct.getTick());
    }

    public void reject() {
        sct.reject(method);
        scsm.notify(false, sct.getTick());
    }

    public void timeout() {
        sct.timeout(method);
        scsm.notify(false, sct.getTick());
    }

    public void error() {
        sct.error(method);
        scsm.notify(false, sct.getTick());
    }

    @Override
    public void success() {
        sct.success(method);
        scsm.notify(true, sct.getTick());
    }

    @Override
    public void shortcircuited(ShortCircuitType shortCircuitType) {
        if (null == shortCircuitType) {
            error();
            return;
        }
        switch (shortCircuitType) {
            case TASK_REJ:
                reject();
                break;
            case TIMEOUT:
                timeout();
                break;
            case FAILURE:
                error();
                break;
            default:
                error();
        }
    }

    @Override
    public void exception(Throwable throwalbe) {
        error();
    }

}
