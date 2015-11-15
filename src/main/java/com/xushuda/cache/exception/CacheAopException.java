package com.xushuda.cache.exception;

/**
 * all the exception should be derived from this class
 * 
 * @author xushuda
 *
 */
public abstract class CacheAopException extends Exception {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public CacheAopException(String message) {
        super(message);
    }
}
