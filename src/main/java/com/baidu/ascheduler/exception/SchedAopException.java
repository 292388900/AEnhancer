package com.baidu.ascheduler.exception;

/**
 * all the exception should be derived from this class
 * 
 * @author xushuda
 *
 */
public abstract class SchedAopException extends Exception {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public SchedAopException(String message) {
        super(message);
    }

    public SchedAopException(Exception e) {
        super(e);
    }
}
