package com.baidu.aenhancer.entry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 输入参数必须是List<Object> 每个object是一次调用返回的结果
 * 
 * @author xushuda
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Collapse {

    String value() default "";
}
