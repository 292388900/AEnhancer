package com.baidu.aenhancer.core.processor.ext.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.conf.runtime.RuntimeConfigManager;
import com.baidu.aenhancer.exception.CodingError;

public class ExecPoolFactory {
    private final static Logger logger = LoggerFactory.getLogger(ExecPoolFactory.class);

    private static volatile ExecPoolFactory instance;
    private final ConcurrentHashMap<String, ExecPool> pools;

    public static ExecPoolFactory getInstance() {
        if (null == instance) {
            synchronized (ExecPoolFactory.class) {
                if (null == instance) {
                    instance = new ExecPoolFactory();
                }
            }
        }
        return instance;
    }

    private ExecPoolFactory() {
        pools = new ConcurrentHashMap<String, ExecPool>();
    }

    /**
     * 
     * @param group
     * @return
     * @throws CodingError
     */
    public ExecPool getExecPool(String group) throws CodingError {
        ExecPool pool = pools.get(group);
        if (null == pool) {
            pools.putIfAbsent(group, RuntimeConfigManager.factory(ExecPool.class, group));
            pool = pools.get(group);
        }
        return pool;
    }
}
