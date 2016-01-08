package com.baidu.ascheduler.exception;

/**
 * 短路信号可以被上级的processor捕获
 * 
 * @author xushuda
 *
 */
public class ShortCircuitExcption extends SchedAopException {

    private static final long serialVersionUID = 1L;

    public ShortCircuitExcption(String message) {
        super(message);
    }

}
