package com.baidu.aenhancer.entry;

public @interface Parallel {

    /**
     * 实现splitProxy的bean，或者相同value值的 @collapse 和 @split 组成的bean
     * 
     * @return bean的名称
     */
    // spliter proxy bean name
    String spliter() default "";

    // group of the exec pool
    String group() default "";
}
