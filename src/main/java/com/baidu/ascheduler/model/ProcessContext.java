package com.baidu.ascheduler.model;

import java.util.Random;

import org.aspectj.lang.ProceedingJoinPoint;

import com.baidu.ascheduler.cache.driver.CacheDriver;
import com.baidu.ascheduler.exception.IllegalParamException;

/**
 * hold thread local variable to provide info of signature and annotation
 * 
 * @author xushuda
 *
 */
public class ProcessContext {

    private final long ctxId;
    // private static final Logger logger = LoggerFactory.getLogger(ProcessContext.class);
    private SignatureInfo signature;
    private AnnotationInfo annotation;
    private final Object[] clonedArgs; // 克隆的原参数
    private ProceedingJoinPoint jp; // join point
    private CacheDriver cacheDriver;

    // private Aggregation batchInvokAggr; // 不包含集合类外的参数

    public ProcessContext(SignatureInfo signature, AnnotationInfo annotation, ProceedingJoinPoint jp) {
        this.signature = signature;
        this.annotation = annotation;
        this.jp = jp;
        clonedArgs = jp.getArgs().clone();
        ctxId = new Random().nextLong();
    }

    public Object getAggrParam(Object[] p) {
        return null;
    }

    public CacheDriver getCacheDriver() {
        return cacheDriver;
    }

    public void setCacheDriver(CacheDriver cacheDriver) {
        this.cacheDriver = cacheDriver;
    }

    /**
     * 获取克隆的原始参数的引用，所以，直接修改这个数组的的内容不会对原对象产生影响<br>
     * 但是是不能修改数组中引用的对象
     * 
     * @return cloned的参数数组
     */
    public Object[] getArgs() {
        return clonedArgs;
    }

    /**
     * 
     * @param args 参数
     * @return 返回按照给入参数调用原方法的结果
     * @throws Throwable 异常
     */
    public Object invokeOrignialMethod(Object[] args) throws Throwable {
        return jp.proceed(args);
    }

    /**
     * 获取批量处理的限制
     * 
     * @return
     */
    public int getBatchSize() {
        return annotation.getBatchSize();
    }

    /**
     * 获取忽略参数列表
     * 
     * @return
     */
    public int[] getIgnoreList() {
        return annotation.getIgnList();
    }

    /**
     * 获取driver的beanName
     * 
     * @return
     */
    public String getDriver() {
        return annotation.getDriverName();
    }

    /**
     * 从参数集合中的一个对象获取key
     * 
     * @param paramElement
     * @return
     * @throws IllegalParamException
     */
    public Object getKeyFromParam(Object paramElement) throws IllegalParamException {
        return annotation.extParam(paramElement);
    }

    /**
     * 从结果的一个对象取得Key
     * 
     * @param resultElement
     * @return
     * @throws IllegalParamException
     */
    public Object getKeyFromResult(Object resultElement) throws IllegalParamException {
        return annotation.extResult(resultElement);
    }

    /**
     * 将原有参数中的聚合类参数替换为入参的对象
     * 
     * @param keys
     * @return
     */
    public Object[] replaceArgsWithKeys(Object keys) {
        clonedArgs[signature.getPosition()] = keys;
        return clonedArgs;
    }

    /**
     * 过期时间，秒
     * 
     * @return
     */
    public int getExpiration() {
        return annotation.getExpiration();
    }

    /**
     * 对于AggrInvok中的集合对象参数
     * 
     * @return
     */
    public Class<?> getAggParamType() {
        return signature.getAggParamType();
    }

    /**
     * 返回值类型
     * 
     * @return
     */
    public Class<?> getRetType() {
        return signature.getRetType();
    }

    /**
     * 获取keySpace
     * 
     * @return
     */
    public String getNameSpace() {
        return annotation.getNameSpace().equals("") ? signature.getSignature() : annotation.getNameSpace();
    }

    /**
     * 是否是聚合式调用
     * 
     * @return
     */
    public boolean aggrInvok() {
        return annotation.aggrInvok();
    }

    public int getRetry() {
        return annotation.getRetryTimes();
    }

    /**
     * 
     * @return
     */
    public boolean relyOnSeqResult() {
        return annotation.isResultSequential();
    }

    public long getCtxId() {
        return ctxId;
    }

    /**
     * ( 获取集合类参数在原参数中的位置
     * 
     * @return
     */
    public int getAggrPosition() {
        return signature.getPosition();
    }

}
