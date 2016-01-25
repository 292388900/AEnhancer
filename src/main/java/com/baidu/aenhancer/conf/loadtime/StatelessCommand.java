package com.baidu.aenhancer.conf.loadtime;

public interface StatelessCommand {
    public Object call(Object...args);
    public Object getBean();
}
