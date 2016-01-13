package com.baidu.aenhancer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.baidu.aenhancer.entry.Enhancer;
import com.baidu.aenhancer.ext.impl.aggr.Aggr;
import com.baidu.aenhancer.ext.impl.aggr.AggrCacher;
import com.baidu.aenhancer.ext.impl.aggr.AggrSpliter;
import com.baidu.aenhancer.ext.impl.aggr.ReturnNull;

@Service
public class BeanMock {
    @Enhancer
    public Integer testGet(Integer x) {
        return ++x;
    }

    @Aggr(param = "T(java.lang.Math).PI", result = "#this.getKey()", nameSpace = "test")
    @Enhancer
    public Map<Integer, String> testGetList(ConcurrentHashMap<Integer, String> param) {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        for (Entry<Integer, String> pi : param.entrySet()) {
            ret.put(pi.getKey(), pi.getKey() + ":" + pi.getValue());
        }
        return ret;
    }

    boolean throwEx = true;

    @Enhancer(retry = 2, fallback = ReturnNull.class)
    public Integer get(Integer x) {
        if (throwEx) {
            throwEx = false;
            throw new RuntimeException("error");
        }
        return x;
    }

    @Enhancer(fallback = ReturnNull.class)
    public int testFallback() {
        throw new RuntimeException();
    }

    @Enhancer
    public Integer getInt() {
        return 5;
    }

    @Aggr(ignList = 0)
    @Enhancer()
    public Integer getInt2(Integer f) {
        return 6;
    }

    @Aggr(cache = "NopDriver")
    @Enhancer()
    public String getStr() {
        return "OK";
    }

    @Aggr(sequential = true, batchSize = 1)
    @Enhancer(group = "testStrs", timeout = 100, cacher = AggrCacher.class, spliter = AggrSpliter.class, parallel = true)
    public List<String> getStrs(String[] args) {
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Arrays.asList(args);
    }

    @Aggr(cache = "")
    @Enhancer(timeout = 100)
    public String timeoutTest() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
        return "test timeout";
    }

    @Aggr(cache = "")
    @Enhancer(timeout = 12000)
    public String notTimeoutTest() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
        return "test not timeout";
    }
}
