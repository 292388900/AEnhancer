package com.baidu.aenhancer.entry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @split 和 @collapse 配合使用，相同的名称组成对应的配对，填入 @Parallel
 * Split修饰的函数，接受的参数是原函数的参数列表 ，返回必须是List<Object[]>，每个element的Object[]对应拆分后的参数列表
 * 
 * @author xushuda
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Split {

    String value() default "";
}
