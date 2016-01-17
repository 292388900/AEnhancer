package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ShortCircuitStateMachineFactory {
    private ConcurrentHashMap<Method, ShortCircuitStateMachine> scsmMap;

    private ShortCircuitStateMachineFactory() {
        scsmMap = new ConcurrentHashMap<Method, ShortCircuitStateMachine>();
    }

    private volatile static ShortCircuitStateMachineFactory instance;

    public static ShortCircuitStateMachineFactory getInstance() {
        if (null == instance) {
            synchronized (ShortCircuitStateMachineFactory.class) {
                if (null == instance) {
                    instance = new ShortCircuitStateMachineFactory();
                }
            }
        }
        return instance;
    }

    public ShortCircuitStateMachine getStateMachine(Method method, int tick) {
        ShortCircuitStateMachine scsm = scsmMap.get(method);
        if (null == scsm) {
            synchronized (method) {
                scsm = scsmMap.get(method);
                if (null == scsm) {
                    scsm = new ShortCircuitStateMachine(method, tick, 10, 0.85, 4, 50, 0, 1);
                    scsmMap.put(method, scsm);
                }
            }
        }
        return scsm;
    }
}
