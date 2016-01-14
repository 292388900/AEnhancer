package com.baidu.aenhancer.entry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.baidu.aenhancer.core.processor.Hooker;
import com.baidu.aenhancer.core.processor.ext.CacheProxy;
import com.baidu.aenhancer.core.processor.ext.Fallbackable;
import com.baidu.aenhancer.core.processor.ext.Hookable;
import com.baidu.aenhancer.core.processor.ext.Splitable;

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

    /**
     * 是否可用并行
     * 
     * @return
     */
    boolean parallel() default false;

    /**
     * 所属的“组”
     * 
     * @return
     */
    String group() default "";

    Class<? extends CacheProxy> cacher() default NULL.class;

    Class<? extends Splitable> spliter() default NULL.class;

    Class<? extends Fallbackable> fallback() default NULL.class;

    /**
     * 默认Hookable的实现
     * 
     * @return
     */
    Class<? extends Hookable> hook() default Hooker.class;

    // 代表null
    interface NULL extends Splitable, CacheProxy, Fallbackable {
    }

}
