package com.baidu.acache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.baidu.acache.entry.Cached;

@Service
public class BeanMock {

    @Cached
    public Integer testGet(Integer x) {
        return ++x;
    }

    @Cached(keyInParam = "T(java.lang.Math).PI", keyInResult = "getKey()", nameSpace = "test")
    public Map<Integer, String> testGetList(ConcurrentHashMap<Integer, String> param) {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        for (Entry<Integer, String> pi : param.entrySet()) {
            ret.put(pi.getKey(), pi.getKey() + ":" + pi.getValue());
        }
        return ret;
    }

    boolean throwEx = true;

    @Cached(retryTimes = 2)
    public Integer get(Integer x) {
        if (throwEx) {
            throwEx = false;
            throw new RuntimeException("error");
        }
        return x;
    }

    @Cached
    public Integer getInt() {
        return 5;
    }

    @Cached(ignList = 0)
    public Integer getInt2(Integer f) {
        return 6;
    }

    @Cached(driver = "NopDriver")
    public String getStr() {
        return "OK";
    }

    @Cached(driver = "NopDriver", resultSequential = true)
    public List<String> getStrs(String[] args) {
        return Arrays.asList(args);
    }
}
