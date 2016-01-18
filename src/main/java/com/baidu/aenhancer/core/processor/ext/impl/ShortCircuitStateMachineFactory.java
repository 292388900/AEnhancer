package com.baidu.aenhancer.core.processor.ext.impl;

import java.io.IOException;
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

    public ShortCircuitStateMachine getStateMachine(Method method, int tick) throws CodingError, IOException {
        ShortCircuitStateMachine scsm = scsmMap.get(method);
        if (null == scsm) {
            synchronized (method) {
                scsm = scsmMap.get(method);
                if (null == scsm) {
                    // scsm = new ShortCircuitStateMachine(method, tick, 10, 0.85, 4, 50, 0, 1);
                    scsm = ConfigManager.getInstance().factory(ShortCircuitStateMachine.class, method, tick);
                    scsmMap.put(method, scsm);
                }
            }
        }
        return scsm;
    }
}
