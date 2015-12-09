package com.baidu.acache;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
    }

}
