package com.baidu.aenhancer.core.processor.ext.impl;

import java.io.IOException;

import com.baidu.aenhancer.core.conf.ConfigManager;
import com.baidu.aenhancer.exception.CodingError;

public class ShortCircuitTickFactory {

    private static volatile ShortCircuitTick instance;

    /**
     * @return
     * @throws IOException 
     * @throws CodingError 
     */
    public static ShortCircuitTick getInstance() throws CodingError, IOException {
        if (null == instance) {
            synchronized (ShortCircuitTick.class) {
                if (null == instance) {
                    // 5000毫秒，最大20个窗口
                    instance = ConfigManager.getInstance().factory(ShortCircuitTick.class);
                }
            }
        }
        return instance;
    }
}
