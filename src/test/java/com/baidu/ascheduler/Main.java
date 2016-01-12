package com.baidu.ascheduler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        BeanMock entry = context.getBean(BeanMock.class);
        entry.getStrs(new String[] { "a", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b",
                "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b" });
        entry.get(3);
    }
}
