package com.baidu.acache.model;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.acache.exception.IllegalParamException;

/**
 * hold thread local variable to provide info of signature and annotation
 * 
 * @author xushuda
 *
 */
public class MethodInfo {

    private static final Logger logger = LoggerFactory.getLogger(MethodInfo.class);
    private SignatureInfo signature;
    private AnnotationInfo annotation;
    private Object[] clonedArgsRef;
    private Object orgAggrArgs;
    private ProceedingJoinPoint jp;

    public MethodInfo(SignatureInfo signature, AnnotationInfo annotation, ProceedingJoinPoint jp) {
        this.signature = signature;
        this.annotation = annotation;
        this.jp = jp;
        clonedArgsRef = jp.getArgs().clone();
        if (annotation.aggrInvok()) {
            orgAggrArgs = clonedArgsRef[signature.getPosition()];
        }
    }

    /**
     * 获取原参数中聚合位置的参数对象
     * 
     * @return 如果不存在Aggr调用，则返回null
     */
    public Object getOrgAggrParam() {
        return orgAggrArgs;
    }

    /**
     * 获取克隆的原始参数的引用，所以，直接修改这个数组的的内容不会对原对象产生影响<br>
     * 但是是不能修改数组中引用的对象
     * 
     * @return cloned的参数数组
     */
    public Object[] getArgs() {
        return clonedArgsRef;
    }

    /**
     * 从原始接口调用，会根据annotation中的重试次数重试
     * 
     * @param args 参数
     * @return 返回按照给入参数调用原方法的结果
     * @throws Throwable 异常
     */
    public Object proceedWith(Object[] args) throws Throwable {
        // 在最后一次重试前都catch所有异常
        for (int retryTimes = annotation.getRetryTimes(); retryTimes > 1; --retryTimes) {
            try {
                jp.proceed(args);
            } catch (Throwable th) {
                logger.info("error whe calling {}, exception: {}, left ret times: {}", signature.getSignature(), th,
                        retryTimes - 1);
            }
        }

        // 最后一次重试如果有异常则抛出
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
        clonedArgsRef[signature.getPosition()] = keys;
        return clonedArgsRef;
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

    /**
     * 
     * @return
     */
    public boolean relyOnSeqResult() {
        return annotation.isResultSequential();
    }

}
