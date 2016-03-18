package com.baidu.aenhancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.baidu.aenhancer.entry.Collapse;
import com.baidu.aenhancer.entry.Enhancer;
import com.baidu.aenhancer.entry.Fallback;
import com.baidu.aenhancer.entry.Parallel;
import com.baidu.aenhancer.entry.Split;
import com.baidu.aenhancer.ext.impl.aggr.Aggr;

@Service("bean1")
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

    @Enhancer(retry = 2, fallback = "ReturnNull")
    public Integer get(Integer x) {
        throw new RuntimeException("error");
    }
    
    @Fallback("wrong")
    public void whatsWrong(){
        System.out.println("some goes wrong");
    }

    @Enhancer(fallback = "wrong")
    public int testFallback(Integer x) {
        try {
            Thread.sleep(000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    @Aggr(sequential = true, aggrSize = 1, cache = "NopDriver")
    @Enhancer( //
    timeout = 100, // 超时时间
    cacher = "aggrCache", // 缓存策略：按集合对象中的元素缓存
    parallel = @Parallel(spliter = "aggr", group = "ServiceGroupA"), // 并行
    fallback = "wrong" // 降级策略
    )
    public List<String> getStrs(String[] args) {
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new RuntimeException();
//        return Arrays.asList(args);
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

    @Enhancer(parallel = @Parallel(spliter = "bean1.spliter"))
    public Integer costomSplit(int start, int end) {
        return end + start;
    }

    @Split
    public List<Object[]> kill(int start, int end) {
        List<Object[]> ret = new ArrayList<Object[]>();
        ret.add(new Object[] { start, start });
        ret.add(new Object[] { end, end });
        return ret;
    }

    @Collapse
    public Integer collapse(List<Integer> result) {
        int x = 0;
        for (Integer i : result) {
            x += i;
        }
        return x;
    }

}
