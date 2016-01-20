package com.baidu.aenhancer;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.aenhancer.core.conf.ConfigManager;
import com.baidu.aenhancer.core.processor.ext.impl.ExecPool;
import com.baidu.aenhancer.core.processor.ext.impl.ShortCircuitStateMachine;
import com.baidu.aenhancer.core.processor.ext.impl.ShortCircuitTick;
import com.baidu.aenhancer.exception.CodingError;

public class Main {
    static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    final static BeanMock entry1 = context.getBean("bean1", BeanMock.class);
    final static BeanMock entry2 = context.getBean("bean2", BeanMock.class);

    // final static BeanMock entry3 = context.getBean("bean2", BeanMock.class);

    public static void main(String[] args) {

        // entry.getStrs(new String[] { "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b"
        // });
        // entry.get(5);
        // entry.testFallback();
        entry2.costomSplit(1, 2);

        // testFac();

    }

    public static void testFac() {

        try {
            ConfigManager.factory(ShortCircuitStateMachine.class, Main.class.getDeclaredMethods()[0], 2);
            ConfigManager.factory(ShortCircuitTick.class);
            ConfigManager.factory(ExecPool.class, "SHARED_POOL");
            ConfigManager.yieldAll();
        } catch (CodingError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testMt() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Method m = entry1.getClass().getMethod("getInt");
                    synchronized (m) {
                        System.out.println("start");
                        Thread.sleep(5000);
                        System.out.println("end");
                    }
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Method m = entry1.getClass().getMethod("getInt");
                    synchronized (m) {
                        System.out.println("start");
                        Thread.sleep(5000);
                        System.out.println("end");
                    }
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
