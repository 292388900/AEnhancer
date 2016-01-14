package com.baidu.aenhancer.core.processor.ext;

/**
 * 
 * @author xushuda
 *
 */
public interface HookProxy extends Hookable {

    public Object call(Object[] param) throws Throwable;

}
