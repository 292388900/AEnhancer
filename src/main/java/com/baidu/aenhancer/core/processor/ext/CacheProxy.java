package com.baidu.aenhancer.core.processor.ext;

/**
 * cache一定在一个单线中
 * 
 * <pre>
 * retrieveFromCache（1） 
 *   － allCached（2） 
 *      IF (2)==no － 返回 （1）的值
 *      IF (2)==yes － 调用后续方法，(1)的值做参数 － cacheNcollapse（4） － 返回（4）的值
 * 
 * @author xushuda
 *
 */
public interface CacheProxy extends Contextural {

    /**
     * 先从cache中读取,
     * 
     * 如果allCached() == true，则返回结果<br>
     * 如果allCached() == false，返回需要传递给原方法的参数
     * 
     * @param param
     * @return
     */
    public Object retrieveFromCache(Object[] param);

    /**
     * 如果全部都命中缓存，则直接返回
     * 
     * @return
     */
    public boolean allCached();

    /**
     * 获得下个process的返回值后会调用这个方法，
     * 
     * 这里可能有两个工作，1合并部分命中的结果集合，2将那些未缓存的放入缓存中
     * 
     * @return 返回给上层的值
     */
    public Object cacheNcollapse(Object resultFromNxtProc);
}
