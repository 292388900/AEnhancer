package com.baidu.aenhancer.exception;

/**
 * Illegal param exception
 * 
 * @author xushuda
 *
 */
public class IllegalParamException extends EnhancerRuntimeException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public IllegalParamException(String message) {
        super(message);
    }
}
