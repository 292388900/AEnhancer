package com.baidu.aenhancer.entry;

public @interface Parallel {

    // spliter proxy bean name
    String spliter() default "";
    
    // group of the exec pool
    String group() default "";
}
