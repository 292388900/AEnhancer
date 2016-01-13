package com.baidu.aenhancer.ext.impl.aggr;

import java.util.Arrays;
import java.util.List;

/**
 * 自带的Nop Driver
 * 
 * @author xushuda
 *
 */
public class NopCacheDriver implements CacheDriver {

    @Override
    public Object get(String key, String nameSpace) {
        return null;
    }

    @Override
    public void set(String key, Object data, int expire, String nameSpace) {
    }

    @Override
    public void setAll(List<String> keys, List<Object> datas, int experiation, String nameSpace) {
    }

    @Override
    public List<Object> getAll(List<String> keys, String nameSpace) {
        return Arrays.asList(new Object[keys.size()]);
    }

    @Override
    public boolean flush(String nameSpace) {
        return true;
    }

    @Override
    public boolean flush(String nameSpace, String key) {
        return true;
    }

    @Override
    public void flushAll() {
    }

}
