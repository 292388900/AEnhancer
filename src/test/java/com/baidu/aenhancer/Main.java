package com.baidu.aenhancer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        // System.out.print((Integer.MAX_VALUE+4) & 0x7FFFFFFF % 10);
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        final BeanMock entry = context.getBean(BeanMock.class);
        entry.get(3);
        entry.get(5);
        entry.get(7);
        entry.testFallback();
        new Thread(new Runnable() {

            @Override
            public void run() {
                entry.get(5);
                entry.get(7);

            }
        }).start();
        System.out.print(entry.costomSplit(1, 2));
    }
}
