package com.baidu.acache.entry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对Cached注解的方法，有如下注意事项：<br>
 * 1、keyInParam和keyInResult使用spring expression language。<br>
 * 对于map类型的对象，上下文对象为一个Map.Entry <br>
 * 对于一般的集合对象，上下文对象为一个元素Element <br>
 * 2、paramK这个方法用于从参数集合中获取key，来访问缓存 , keyInResult这个方法用于从结果集合中获取key，用以缓存数据。<br>
 * 所以对于某数据，从keyInParam，keyInResult得到的key必须一致 <br>
 * 3、keyInParam不为空，keyInResult为空的情况是错误的,因为不可能返回值只有一个key<br>
 * 4、在生成key的过程中，会调用参数（聚合Invok则调用集合element）的hashCode函数，<br>
 * 所以务必确保不在ignList中的参数都正确地重写了hashCode函数，不是简单返回对象的内存地址
 * 
 * @author xushuda
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cached {

    /**
     * the Cache driver used 为实现类的bean name 默认为DefaultCacheDriver
     * 
     * @return
     */
    String driver() default "DefaultCacheDriver";

    /**
     * the expire of the data in second
     * 
     * @return
     */
    int expiration() default 3600;

    /**
     * 从集合类的参数中获取 缓存的key <br>
     * 如果为空，则表示每个集合的元素（element）本身作为key作为参数之一传递给cache driver <br>
     * 对于map类型的对象，上下文对象为一个Map.Entry <br>
     * 对于一般的集合对象，上下文对象为一个元素Element <br>
     * 
     * @return Spring el 表达式
     */
    String keyInParam() default "";

    /**
     * 从集合类的结果集中获取缓存的key 如果与keyInParam()都为空， 则表示该方法不使用 批量请求方式 <br>
     * 对于map类型的对象，上下文对象为一个Map.Entry <br>
     * 对于一般的集合对象，上下文对象为一个元素Element <br>
     * 
     * @return Spring el 表达式
     */
    String keyInResult() default "";

    /**
     * 只有当批量请求有最大请求条数限制的时候才使用这个字段 <br>
     * 
     * @return
     */
    int batchLimit() default 0;

    /**
     * 忽略的参数列表 (数字代表第几个参数)，如果忽略则对应位置参数不加入key的计算<br>
     * 初始位置为 0(第一个参数) <br>
     * 比如一些获取限制参数,过滤参数等等 <br>
     * CAUTION: 如果使用了ignList(参数是状态信息)，可能需要在上层手动过滤一下
     * 
     * @return
     */
    int[] ignList() default {};

    /**
     * key的命名空间，默认空字符串则为函数签名
     */
    String nameSpace() default "";
}
