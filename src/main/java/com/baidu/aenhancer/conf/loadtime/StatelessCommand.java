package com.baidu.aenhancer.conf.loadtime;

/**
 * 一般是代表一个bean的某个被注解的方法对象
 * 
 * @author xushuda
 *
 */
public interface StatelessCommand {
    // 被调用的接口，真实的方法
    public Object call(Object...args);

    // 注解的方法所在的bean的引用
    public Object getBean();
}
