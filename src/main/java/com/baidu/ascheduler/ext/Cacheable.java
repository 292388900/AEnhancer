package com.baidu.ascheduler.ext;

import com.baidu.ascheduler.exception.SchedAopException;

/**
 * cache一定在一个单线中
 * 
 * <pre>
 * retrieveFromCache（1） － shortcircuit（2） 
 * IF (2)==no － 返回 （1）的值
 * IF (2)==yes － beforeNxtProc（3） － afterNxtProc（4） － 返回（4）的值
 * 
 * @author xushuda
 *
 */
public interface Cacheable extends Contextural {

    /**
     * 如果
     * 
     * @param param
     * @return
     * @throws SchedAopException 
     */
    public Object retrieveFromCache(Object[] param) throws SchedAopException;

    /**
     * 是否直接短路返回
     * 
     * @return
     */
    public boolean shortcircuit();

    /**
     * 如果不短路，这个方法需要返回传递给下个processor的参数
     * 
     * @return
     */
    public Object[] beforeNxtProc() throws SchedAopException;

    /**
     * 获得下个process的返回值后会调用这个方法，
     * 
     * 这里可能有两个工作，1合并部分命中的结果集合，2将那些未缓存的放入缓存中
     * 
     * @return 返回给上层的值
     */
    public Object afterNxtProc(Object resultFromNxtProc) throws SchedAopException;
}
