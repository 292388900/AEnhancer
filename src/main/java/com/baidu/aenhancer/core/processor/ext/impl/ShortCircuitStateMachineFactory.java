package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.baidu.aenhancer.core.conf.ConfigManager;
import com.baidu.aenhancer.exception.CodingError;

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

    public ShortCircuitStateMachine getStateMachine(Method method, int tick) throws CodingError {
        if (null == method) {
            throw new NullPointerException("method is null");
        }
        ShortCircuitStateMachine scsm = scsmMap.get(method);
        if (null == scsm) {
            scsmMap.putIfAbsent(method, ConfigManager.factory(ShortCircuitStateMachine.class, method, tick));
        }
        return scsmMap.get(method);
    }
}
