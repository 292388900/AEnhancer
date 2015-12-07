package com.baidu.acache.driver;

import java.util.List;

/**
 * CacheDriver nameSpace 这里只是一个最细粒度的定义，默认的命名空间是方法的签名<br>
 * 所有key的类型都是String，所有缓存数据的类型都是Object，如果实现类需要对不同对象做不同处理，应用RTTI<br>
 * 这里所有的接口都没有抛出任何受检异常，因为应用方不知道如何对不同的异常做处理，所以，所有受检异常都应该由实现类处理<br>
 * 抛出runtimeException的情况，框架会捕获并且recover整个请求（不使用缓存）
 * 
 * <pre>
 * 相当于二级key的结构:
 *             |-- key1:data
 * nameSpace1->|-- key2:data 
 *             |-- key3:data 
 *             |...
 * nameSpace2->|-- key1:data
 * 
 * @author xushuda
 *
 */
public interface CacheDriver {

    /**
     * if the data is not in cache ,should return null
     * 
     * @param key key
     * @param nameSpace key的命名空间
     * @return 返回需要的对象
     */
    public Object get(String key, String nameSpace);

    /**
     * set the data and key to cache
     * 
     * @param key
     * @param data 数据
     * @param expire 缓存时效
     */
    public void set(String key, Object data, int expire, String nameSpace);

    /**
     * set the data according to key and value
     * 
     * @param datas key->data
     * @param experiation
     */
    public void setAll(List<String> keys, List<Object> datas, int experiation, String nameSpace);

    /**
     * retrieve all the data from keys <br>
     * the return data's size must be equals to keys' size <br>
     * if the value for a id doesn't exists ,should return null<br>
     * so the return list should have exactly the same order as param<br>
     * id key: id1,id2,id3... return: val1,val2,val3,val4
     * 
     * @param keys
     * @return 获取所有对象，返回List的大小应该跟keys大小一样，未缓存的为null
     */
    public List<Object> getAll(List<String> keys, String nameSpace);

    /**
     * flush one nameSpace
     * 
     * @param nameSpace
     * @return 是否成功刷新
     */
    public abstract boolean flush(String nameSpace);

    /**
     * 刷新某个NmaeSpace下的某个key
     * 
     * @param nameSpace
     * @param key
     * @return
     */
    public abstract boolean flush(String nameSpace, String key);

    /**
     * flushAll
     */
    public abstract void flushAll();
}
