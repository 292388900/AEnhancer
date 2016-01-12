package com.baidu.ascheduler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.baidu.ascheduler.entry.Sched;
import com.baidu.ascheduler.ext.impl.aggr.Aggr;
import com.baidu.ascheduler.ext.impl.aggr.AggrCacher;
import com.baidu.ascheduler.ext.impl.aggr.AggrSpliter;

@Service
public class BeanMock {

    @Sched
    public Integer testGet(Integer x) {
        return ++x;
    }

    @Aggr(param = "T(java.lang.Math).PI", result = "#this.getKey()", nameSpace = "test")
    @Sched
    public Map<Integer, String> testGetList(ConcurrentHashMap<Integer, String> param) {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        for (Entry<Integer, String> pi : param.entrySet()) {
            ret.put(pi.getKey(), pi.getKey() + ":" + pi.getValue());
        }
        return ret;
    }

    boolean throwEx = true;

    @Sched(retry = 2)
    public Integer get(Integer x) {
        if (throwEx) {
            throwEx = false;
            throw new RuntimeException("error");
        }
        return x;
    }

    @Sched
    public Integer getInt() {
        return 5;
    }

    @Aggr(ignList = 0)
    @Sched()
    public Integer getInt2(Integer f) {
        return 6;
    }

    @Aggr(cache = "NopDriver")
    @Sched()
    public String getStr() {
        return "OK";
    }

    @Aggr(sequential = true, batchSize = 1)
    @Sched(timeout = 100, cacher = AggrCacher.class, spliter = AggrSpliter.class, parallel = true)
    public List<String> getStrs(String[] args)  {
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Arrays.asList(args);
    }

    @Aggr(cache = "")
    @Sched(timeout = 100)
    public String timeoutTest() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
        return "test timeout";
    }

    @Aggr(cache = "")
    @Sched(timeout = 12000)
    public String notTimeoutTest() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
        return "test not timeout";
    }
}
