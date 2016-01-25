package com.baidu.aenhancer.conf.loadtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public interface LoadtimeMethodAnnotationParser<T extends Annotation> {

    public void parse(T annotation, Method method, DefaultListableBeanFactory beanFac, String beanName);
}
