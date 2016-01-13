package com.baidu.aenhancer.core.processor.ext;

public interface FallbackProxy extends Fallbackable {

    public Object fallback(Object[] param);
}
