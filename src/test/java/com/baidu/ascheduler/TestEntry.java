package com.baidu.ascheduler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.ascheduler.entry.Sched;
import com.baidu.ascheduler.ext.impl.aggr.Aggr;

public class TestEntry {

    /**
     * 手动读取context并加载
     * 
     * @param args
     */
    public void testBasic() {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        BeanMock entry = context.getBean(BeanMock.class);
        Integer num = 5;
        entry.testGet(num);
        ConcurrentHashMap<Integer, String> param = new ConcurrentHashMap<Integer, String>();
        param.put(5, "sd");
        param.put(6, "dde");
        entry.testGetList(param);
        entry.get(4);
        entry.getInt();
        entry.getInt2(4);
        entry.getStr();
        entry.getStrs(new String[] { "a", "b" });
        entry.timeoutTest();
        entry.notTimeoutTest();
    }

    @Aggr(cache = "NopDriver", sequential = true)
    @Sched(timeout = 100)
    public List<String> getStrs(String[] args) {
        return Arrays.asList(args);
    }

    public static void main(String[] args) throws SecurityException, NoSuchMethodException {
        Method[] a = TestEntry.class.getMethods();
        Method[] b = BeanMock.class.getMethods();
        Method x = null;
        Method y = null;
        for (Method i : a) {
            if (i.getName().equals("getStrs")) {
                x = i;
            }
        }
        for (Method i : a) {
            if (i.getName().equals("getStrs")) {
                y = i;
            }
        }
        for (int i = 0; i < x.getParameterTypes().length; i++) {
            if (!x.getParameterTypes()[i].equals(y.getParameterTypes()[i])) {
                System.out.print(1);
            }
        }

        System.out.println(x.getTypeParameters().equals(y.getTypeParameters()));
        System.out.println(a.equals(b));
    }
}
