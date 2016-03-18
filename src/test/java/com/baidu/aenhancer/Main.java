package com.baidu.aenhancer;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.aenhancer.conf.runtime.RuntimeConfigManager;
import com.baidu.aenhancer.core.processor.ext.impl.ExecPool;
import com.baidu.aenhancer.core.processor.ext.impl.ShortCircuitStateMachine;
import com.baidu.aenhancer.core.processor.ext.impl.ShortCircuitTick;
import com.baidu.aenhancer.exception.CodingError;

public class Main {
    static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    final static BeanMock entry1 = context.getBean("bean1", BeanMock.class);
    final static BeanMock2 entry2 = context.getBean("bean2", BeanMock2.class);

    // final static BeanMock entry3 = context.getBean("bean2", BeanMock.class);

    public static void main(String[] args) {
        entry1.getStrs(new String[] { "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b" });
        entry1.get(5);
        int x = entry1.testFallback(1);
        System.out.println("with null return, primitive type int value is " + x);
        entry1.costomSplit(1, 2);
        entry2.test();

        // testFac();

    }

    public static void testFac() {

        try {
            RuntimeConfigManager.factory(ShortCircuitStateMachine.class, Main.class.getDeclaredMethods()[0], 2);
            RuntimeConfigManager.factory(ShortCircuitTick.class);
            RuntimeConfigManager.factory(ExecPool.class, "SHARED_POOL");
            RuntimeConfigManager.yieldAll();
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
