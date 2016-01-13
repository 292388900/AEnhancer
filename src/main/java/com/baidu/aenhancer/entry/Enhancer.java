package com.baidu.aenhancer.entry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.baidu.aenhancer.core.processor.ext.Cacheable;
import com.baidu.aenhancer.core.processor.ext.Fallbackable;
import com.baidu.aenhancer.core.processor.ext.Splitable;

/**
 * 对Cached注解的方法，有如下注意事项：<br>
 * 1、keyInParam和keyInResult使用spring expression language。<br>
 * 对于map类型的对象，上下文对象为一个Map.Entry <br>
 * 对于一般的集合对象，上下文对象为一个元素Element <br>
 * 2、paramK这个方法用于从参数集合中获取key，来访问缓存 , keyInResult这个方法用于从结果集合中获取key，用以缓存数据。<br>
 * 所以对于某数据，从keyInParam，keyInResult得到的key必须一致 <br>
 * 3、keyInParam不为空，keyInResult为空且keyInResultSeq=false的情况是错误的,因为无法从原函数的返回集合展开进行缓存<br>
 * 4、在生成key的过程中，会调用参数（聚合Invok则调用参数的每个element）的hashCode()函数，<br>
 * 所以务必确保不在ignList中的参数都正确地重写了hashCode函数，不是简单返回对象的内存地址
 * 
 * @author xushuda
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Enhancer {

    /**
     * 重试次数，默认为一次，即：不重试，在重试的过程中（除去最后一次调用，最后一次调用还是会“诚实”地抛出异常）会catch所有的异常<br>
     * ，所以，如果希望对受检异常的情况做特殊的重试处理，请不要使用这个参数
     * 
     * @return 重试次数
     */
    int retry() default 1;

    /**
     * 超时时间，毫秒，小于等于0代表不设置超时时间
     * 
     * 可控制任意函数调用的超时，但是会增加系统负担（多线程）
     * 
     * @return
     */
    int timeout() default 0;

    boolean parallel() default false;

    String group() default "";

    Class<? extends Cacheable> cacher() default NULL.class;

    Class<? extends Splitable> spliter() default NULL.class;

    Class<? extends Fallbackable> fallback() default NULL.class;

    // 代表null
    interface NULL extends Splitable, Cacheable, Fallbackable {
    }

}
