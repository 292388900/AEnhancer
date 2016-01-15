package com.baidu.aenhancer.core.processor.ext;

import java.lang.reflect.Method;

import com.baidu.aenhancer.core.processor.ShortCircuitTick;

public interface ShortCircuitable extends Contextural {

    public boolean shortcircuit(Method method, ShortCircuitTick tick);
}
