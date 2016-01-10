package com.baidu.ascheduler.exception;

import com.baidu.ascheduler.context.ShortCircuitType;

/**
 * 短路信号可以被上级的processor捕获
 * 
 * @author xushuda
 *
 */
public class ShortCircuitExcption extends SchedAopException {

    private static final long serialVersionUID = 1L;
    private ShortCircuitType type;

    public ShortCircuitExcption(String message, ShortCircuitType type) {
        super(message);
        this.type = type;
    }

    public ShortCircuitType getShortCircuitType() {
        return type;
    }
}
