package com.baidu.aenhancer.exception;

import com.baidu.aenhancer.core.context.ShortCircuitType;

/**
 * 短路信号可以被上级的processor捕获
 * 
 * @author xushuda
 *
 */
public class ShortCircuitExcption extends EnhancerCheckedException {

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
