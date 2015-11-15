package com.xushuda.cache.exception;

/**
 * Illegal param exception
 * 
 * @author xushuda
 *
 */
public class IllegalParamException extends CacheAopException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public IllegalParamException(String message) {
        super(message);
    }
}
