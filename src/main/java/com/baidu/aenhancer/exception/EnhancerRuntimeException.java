package com.baidu.aenhancer.exception;

/**
 * all the exception should be derived from this class
 * 
 * @author xushuda
 *
 */
public class EnhancerRuntimeException extends RuntimeException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public EnhancerRuntimeException(String message) {
        super(message);
    }

    public EnhancerRuntimeException(Throwable e) {
        super(e);
    }

    public EnhancerRuntimeException(String message, Throwable e) {
        super(message, e);
    }
}
