package com.xushuda.cache;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestEntry {
    /**
     * 手动读取context并加载
     * 
     * @param args
     */
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        TestBean entry = context.getBean(TestBean.class);
        Integer num = 5;
        System.out.println(entry.testGet(num));
        System.out.println(entry.testGet(num));
    }
}
