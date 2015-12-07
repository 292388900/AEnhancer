package com.baidu.acache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.baidu.acache.entry.Cached;

@Service
public class TestBean {

    @Cached
    public Integer testGet(Integer x) {
        return ++x;
    }

    @Cached(keyInParam = "getKey()",keyInResult="getKey()",nameSpace="test")
    public Map<Integer, String> testGetList(ConcurrentHashMap<Integer, String> param) {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        for (Entry<Integer, String> pi : param.entrySet()) {
            ret.put(pi.getKey(), pi.getKey() + ":" + pi.getValue());
        }
        return ret;
    }

}
