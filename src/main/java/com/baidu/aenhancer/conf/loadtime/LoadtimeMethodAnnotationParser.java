package com.baidu.aenhancer.conf.loadtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * 在加载完bean之后解析注解，对应的实现类需要作为bean被spring context加载
 * 
 * @author xushuda
 *
 * @param <T>
 */
public interface LoadtimeMethodAnnotationParser<T extends Annotation> {

    public void parse(T annotation, Method method, DefaultListableBeanFactory beanFac, String beanName);
}
