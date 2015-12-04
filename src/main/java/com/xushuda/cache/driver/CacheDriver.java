package com.xushuda.cache.driver;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * CacheDriver
 * 
 * <pre>
 * 相当于二级key的结构:
 *            |-- key1:data
 * keySpace1->|-- key2:data 
 *            |-- key3:data
 *            |...
 * keySpace2->|-- key1:data
 * 
 * @author xushuda
 *
 */
public interface CacheDriver {

    /**
     * if the data is not in cache ,should return null
     * 
     * @param id
     * @return
     */
    public Object retrieve(String key, String keySpace);

    /**
     * set the data and key to cache
     * 
     * @param id
     * @param data
     * @param expire
     */
    public void set(String key, Object data, int expire, String keySpace);

    /**
     * set the data according to key and value
     * 
     * @param datas key->data
     * @param experiation
     */
    public void setAll(Map<String, Serializable> datas, int experiation, String keySpace);

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
    public List<Object> getAll(String[] keys, String keySpace);

    /**
     * flush one keySpace
     * 
     * @param key
     * @return
     */
    public abstract boolean flush(String keySpace);

    /**
     * flushAll
     */
    public abstract void flushAll();
}
