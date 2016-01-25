package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.baidu.aenhancer.conf.runtime.RuntimeConfigManager;
import com.baidu.aenhancer.exception.CodingError;

public class ShortCircuitStateMachineFactory {
    private static ConcurrentHashMap<Method, ShortCircuitStateMachine> scsmMap;
    static {
        scsmMap = new ConcurrentHashMap<Method, ShortCircuitStateMachine>();
    }

    public static ShortCircuitStateMachine getStateMachine(Method method, int tick) throws CodingError {
        if (null == method) {
            throw new NullPointerException("method is null");
        }
        if (null == scsmMap.get(method)) {
            scsmMap.putIfAbsent(method, RuntimeConfigManager.factory(ShortCircuitStateMachine.class, method, tick));
        }
        return scsmMap.get(method);
    }
}
