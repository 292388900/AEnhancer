package com.baidu.acache.processor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.acache.entry.Cached;
import com.baidu.acache.exception.IllegalParamException;
import com.baidu.acache.model.Aggregation;
import com.baidu.acache.model.AnnotationInfo;
import com.baidu.acache.model.MethodInfo;
import com.baidu.acache.model.SignatureInfo;

/**
 * the processor 解析函数签名，注解等等
 * 
 * @author xushuda
 *
 */
public final class CacheFrontProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CacheFrontProcessor.class);

    /**
     * 解析 函数签名
     * 
     * @param jp
     * @return
     * @throws IllegalParamException
     */
    private SignatureInfo parseSignature(ProceedingJoinPoint jp, boolean useAggrInvok) throws IllegalParamException {
        Class<?> retType = ((MethodSignature) jp.getSignature()).getReturnType();
        Class<?>[] paramTypes = ((MethodSignature) jp.getSignature()).getParameterTypes();
        Class<?> aggParamType = null;
        int aggregation = 0;
        int position = 0; // the position of the aggregation

        if (null != paramTypes && useAggrInvok) {
            for (Class<?> paramType : paramTypes) {
                if (Aggregation.isAggregationType(paramType)) {
                    // fail fast 不符合期望
                    if (++aggregation > 1) {
                        // should not reach here
                        // 这个异常不应该在发行版本中出现，一定是annotation的参数不正确（与signature不符合）
                        throw new IllegalParamException("at most one Aggregation is allowed in param");
                    }
                    aggParamType = paramType;
                }
                // aggregation = 0 表示还没有聚合类，position递增，
                // aggregation = 1 表示有聚合类 position指向当前聚合类在参数中的位置
                position += 1 - aggregation;
            }
        }
        return new SignatureInfo(retType, aggParamType, position, jp.getSignature());
    }

    /**
     * parse Annotation
     * 
     * @param cached
     * @return annotationInfo
     * @throws IllegalParamException
     */
    private AnnotationInfo parseAnnotation(Cached cached) throws IllegalParamException {
        return new AnnotationInfo(cached);
    }

    /**
     * validate 函数签名与注解是否有效并且正确
     * 
     * @param sig
     * @param anno
     * @throws IllegalParamException
     */
    private void validate(SignatureInfo sig, AnnotationInfo anno) throws IllegalParamException {
        // fail fast
        if (anno.aggrInvok()) { // 如果首先是聚合类调用
            if (anno.isResultSequential()) { // 如果是顺序的聚合类调用
                if (!sig.sequentialAggrAccessible()) {
                    throw new IllegalParamException("the annotation constraints ret type and aggr param is sequential");
                }
            } else if (!sig.aggrAccessible()) { // 不是顺序的调用
                throw new IllegalParamException("signatue conflict with annotation about weather use aggr invocation");
            }
        }
        if (!sig.aggrAccessible() && anno.getBatchSize() > 0) {
            throw new IllegalParamException(
                    " error annotation, if a method's signature is not aggregation accessible, can't use batch ");
        }
    }

    /**
     * 
     * 
     * @param jp
     * @param cached
     * @return
     * @throws IllegalParamException
     */
    public MethodInfo preProcess(ProceedingJoinPoint jp, Cached cached) throws IllegalParamException {
        // 解析注解
        AnnotationInfo annotation = parseAnnotation(cached);
        // 解析函数签名
        SignatureInfo signature = parseSignature(jp, annotation.aggrInvok());
        // validate the signature and annotation
        validate(signature, annotation);
        // 生成methodInfo对象
        MethodInfo methodInfo = new MethodInfo(signature, annotation, jp);
        // fail fast,在这之前抛出的异常都是由于编码的错误，所以，不应该捕获
        // log
        logger.info("success getting methodInfo {}", methodInfo);
        return methodInfo;

    }
}
