package com.baidu.aenhancer.ext.impl.aggr;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 聚合类方式的请求，可以配置batchSize和spEL从集合中获取参数
 * 
 * @author xushuda
 *
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aggr {
    /**
     * 对Aggr注解的方法，有如下注意事项：<br>
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
    // ------------------- AGGR----------------------
    /**
     * 默认不同。所以用keyInResult来展开数据进行缓存,对于聚合类调用，必须满足：keyInResult不为空，或者keyInResultSeq不为false
     * 
     * @return 原方法返回的数据是不是跟param中的顺序相同
     */
    boolean sequential() default false;

    /**
     * 只有当批量请求有最大请求条数限制的时候才使用这个字段 <br>
     * 
     * @return 批量查询的一次请求大小限制
     */
    int batchSize() default 0;

    /**
     * 从集合类的参数中获取 缓存的key <br>
     * 如果为空，则表示每个集合的元素（element）本身作为key作为参数之一传递给cache driver <br>
     * 对于map类型的对象，上下文对象为一个Map.Entry <br>
     * 对于一般的集合对象，上下文对象为一个元素Element <br>
     * 
     * @return Spring el 表达式
     */
    String param() default "";

    /**
     * 从集合类的结果集中获取缓存的key 如果与keyInParam()都为空， 则表示该方法不使用 聚合请求方式 <br>
     * 对于map类型的对象，上下文对象为一个Map.Entry <br>
     * 对于一般的集合对象，上下文对象为一个元素Element <br>
     * 
     * @return Spring el 表达式
     */
    String result() default "";

    /**
     * the Cache driver used 为实现类的bean name 默认为DefaultCacheDriver
     * 
     * @return driver的bean name
     */
    String cache() default "DefaultCacheDriver";

    /**
     * the expire of the data in second
     * 
     * @return 超时时间 秒
     */
    int expiration() default 3600;

    /**
     * key的命名空间，默认空字符串则为函数签名
     * 
     * @return nameSpace
     */
    String nameSpace() default "";

    /**
     * 忽略的参数列表 (数字代表第几个参数)，如果忽略则对应位置参数不加入key的计算<br>
     * 初始位置为 0(第一个参数) <br>
     * 比如一些获取限制参数,过滤参数等等 <br>
     * 
     * @return 忽略参数列表
     */
    int[] ignList() default {};

    /**
     * driver
     * 
     * @return
     */
    String driver() default "";
}
