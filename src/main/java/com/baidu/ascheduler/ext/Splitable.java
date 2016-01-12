package com.baidu.ascheduler.ext;

import java.util.List;

import com.baidu.ascheduler.exception.SchedAopException;

public interface Splitable extends Contextural {

    public Object collapse(List<Object> result) throws SchedAopException;

    public List<Object[]> split(Object[] args) throws SchedAopException;
}