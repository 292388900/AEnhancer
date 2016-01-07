package com.baidu.ascheduler.exception;

/**
 * Illegal param exception
 * 
 * @author xushuda
 *
 */
public class IllegalParamException extends SchedAopException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public IllegalParamException(String message) {
        super(message);
    }
}
