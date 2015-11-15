package com.xushuda.cache.driver;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * CacheDriver
 * 
 * @author xushuda
 *
 */
public interface CacheDriver {

    /**
     * generate the identifier for a data <br>
     * args contain all the argument, including that in ignore list<br>
     * 对于Aggregation类，这个参数包含所有非aggr类的参数，和aggr类中的单个参数<br>
     * 一般来说，对Object对象的处理，生成签名都会调用toString一类的方法，注意参数类中相应方法的正确性<br>
     * TODO 用hashCode?
     * @param args
     * @return
     */
    public String id(Object...args);

    /**
     * if the data is not in cache ,should return null
     * 
     * @param id
     * @return
     */
    public Object retrieve(String id);

    /**
     * set the data and key to cache
     * 
     * @param id
     * @param data
     * @param expire
     */
    public void set(String id, Object data, int expire);

    /**
     * set the data according to key and value
     * 
     * @param datas
     * @param experiation
     */
    public void setAll(Map<String, Serializable> datas, int experiation);

    /**
     * retrieve all the data from keys <br>
     * the return data's size must be equals to keys' size <br>
     * if the value for a id doesn't exists ,should return null<br>
     * so the return list should have exactly the same order as param<br>
     * id key: id1,id2,id3... return: val1,val2,val3,val4
     * 
     * @param keys
     * @return
     */
    public List<Object> getAll(String[] ids);

    /**
     * flush one key
     * 
     * @param key
     * @return
     */
    public abstract boolean flush(String key);

    /**
     * flushAll
     */
    public abstract void flushAll();
}
