package com.baidu.aenhancer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("bean2")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BeanMockInherit extends BeanMock {

    public BeanMockInherit(){
        System.out.println("123131");
    }
}
