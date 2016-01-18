package com.baidu.aenhancer;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.aenhancer.core.conf.ConfigManager;
import com.baidu.aenhancer.core.processor.ext.impl.ShortCircuitStateMachine;
import com.baidu.aenhancer.core.processor.ext.impl.ShortCircuitTick;
import com.baidu.aenhancer.exception.CodingError;

public class Main {

    public static void main(String[] args) {
        // System.out.print((Integer.MAX_VALUE+4) & 0x7FFFFFFF % 10);
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        final BeanMock entry = context.getBean(BeanMock.class);
        entry.getStrs(new String[] { "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b" });
        entry.get(5);
        entry.testFallback();
        entry.costomSplit(1, 2);
        try {
            ConfigManager cm = ConfigManager.getInstance();
            cm.factory(ShortCircuitStateMachine.class, Main.class.getDeclaredMethods()[0], 2);
            cm.factory(ShortCircuitTick.class);
        } catch (CodingError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
