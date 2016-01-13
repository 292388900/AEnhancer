package com.baidu.aenhancer.exception;

/**
 * the storage is in unexpected state <br>
 * may be caused by cacheDriver's error return value
 * 
 * @author xushuda
 *
 */
public class UnexpectedStateException extends EnhancerRuntimeException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public UnexpectedStateException(String message) {
        super(message);
    }
    
    public UnexpectedStateException(Exception e){
        super(e);
    }

}
