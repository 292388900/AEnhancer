package com.baidu.aenhancer.exception;

public class EnhancerCheckedException extends Exception {
    private static final long serialVersionUID = 1L;

    public EnhancerCheckedException(String message) {
        super(message);
    }

    public EnhancerCheckedException(String message, Throwable e) {
        super(message, e);
    }
}
