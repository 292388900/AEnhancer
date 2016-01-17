package com.baidu.aenhancer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        // System.out.print((Integer.MAX_VALUE+4) & 0x7FFFFFFF % 10);
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        final BeanMock entry = context.getBean(BeanMock.class);
        entry.getStrs(new String[] { "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b" });

        entry.get(3);
        entry.get(5);
        entry.testFallback();
        new Thread(new Runnable() {

            @Override
            public void run() {
                entry.get(1);
                entry.get(9);

            }
        }).start();
        entry.costomSplit(1, 2);
    }
}
