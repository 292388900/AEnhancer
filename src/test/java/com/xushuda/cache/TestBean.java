package com.xushuda.cache;

import org.springframework.stereotype.Service;

import com.xushuda.cache.entry.Cached;

@Service
public class TestBean {

    @Cached
    public Integer testGet(Integer x) {
        return ++x;
    }

  
}
