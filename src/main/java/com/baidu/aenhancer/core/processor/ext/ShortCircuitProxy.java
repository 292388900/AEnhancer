package com.baidu.aenhancer.core.processor.ext;

import com.baidu.aenhancer.core.context.ShortCircuitType;

public interface ShortCircuitProxy extends Contextural {

    public boolean shortcircuit();

    public void shortcircuited(ShortCircuitType shortCircuitType);

    public void exception(Throwable throwalbe);

    public abstract void success();

}
