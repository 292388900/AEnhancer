package com.baidu.aenhancer.core.processor.ext;

import com.baidu.aenhancer.entry.Fallback;

public interface FallbackProxy extends Contextural {

    @Fallback
    public Object fallback(Object[] param);
}
