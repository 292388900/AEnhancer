package com.baidu.aenhancer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("bean2")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BeanMock2 {

    public BeanMock2(){
        System.out.println("123131");
    }
    public void test(){
        System.out.println("end test");
    }
}
