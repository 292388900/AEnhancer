package com.xushuda.cache.processor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xushuda.cache.entry.Cached;
import com.xushuda.cache.exception.CacheAopException;
import com.xushuda.cache.exception.IllegalParamException;
import com.xushuda.cache.model.Aggregation;
import com.xushuda.cache.model.AnnotationInfo;
import com.xushuda.cache.model.Info;
import com.xushuda.cache.model.SignatureInfo;

/**
 * the processor
 * 
 * @author xushuda
 *
 */
@Service
public class CacheFrontProcessor {

    @Autowired
    private CacheDataProcessor cacheProcessor;

    private static final Logger logger = LoggerFactory.getLogger(CacheFrontProcessor.class);

    /**
     * 解析 函数签名
     * 
     * @param jp
     * @return
     * @throws IllegalParamException
     */
    private SignatureInfo parseSignature(ProceedingJoinPoint jp, boolean useBatchCall) throws IllegalParamException {
        Class<?> retType = ((MethodSignature) jp.getSignature()).getReturnType();
        Class<?>[] paramTypes = ((MethodSignature) jp.getSignature()).getParameterTypes();
        Class<?> aggParamType = null;
        int aggregation = 0;
        int position = 0; // the position of the aggregation

        if (null != paramTypes && useBatchCall) {
            for (Class<?> paramType : paramTypes) {
                if (Aggregation.isAggregationType(paramType)) {
                    // fail fast 不符合期望
                    if (++aggregation > 1) {
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
     * @return
     */
    private AnnotationInfo parseAnnotation(Cached cached) {
        return new AnnotationInfo(cached.paramK(), cached.resultK(), cached.batchLimit(), cached.driverClass(),
                cached.expiration(), cached.ignList());
    }

    /**
     * validate
     * 
     * @param sig
     * @param anno
     * @throws IllegalParamException
     */
    private void validate(SignatureInfo sig, AnnotationInfo anno) throws IllegalParamException {
        // fail fast
        if (!sig.aggrAccessible() && anno.aggrInvok()) {
            throw new IllegalParamException("signatue conflict with annotation about weather use aggr invocation");
        }
        if (!sig.aggrAccessible() && anno.getBatchSize() > 0) {
            throw new IllegalParamException(
                    " error annotation, if a method's signature is not aggregation accessible, can't use batch ");
        }
    }

    /**
     * around 入口
     * 
     * @param jp
     * @param cached
     * @return
     * @throws Throwable
     */
    public Object aopAround(ProceedingJoinPoint jp, Cached cached) throws Throwable {
        try {
            // 解析注解
            AnnotationInfo annotation = parseAnnotation(cached);
            // 解析函数签名
            SignatureInfo signature = parseSignature(jp, annotation.aggrInvok());
            // set the annotation and signature to the threadLocal variable
            Info.init(signature, annotation);
            // validate the signature and annotation
            validate(signature, annotation);

            // 根据不同的
            if (!annotation.aggrInvok()) {
                logger.info("start to retrive data from {} ", signature.toString());
                return cacheProcessor.processNormal(annotation, jp);
            } else {
                return cacheProcessor.processAggregated(signature, annotation, jp);
            }
        } catch (CacheAopException exp) {
            // be careful, this kind of exception may caused by your incorrect code
            logger.error("error occors in cache aop , caused by :", exp);
            if (Info.getAnnotation().getBatchSize() > 0) {
                return cacheProcessor.processAggregatedWithoutCache(Info.getSignature(), Info.getAnnotation(), jp);
            }
            // return the original call
            return jp.proceed(jp.getArgs());
        } catch (RuntimeException rtExp) {
            // swallow the runtime exception
            logger.error("runtime exception occurs in cache aop , caused by :", rtExp);
            // be careful about this kind of exception, the cache server may just crash
            if (Info.getAnnotation().getBatchSize() > 0) {
                return cacheProcessor.processAggregatedWithoutCache(Info.getSignature(), Info.getAnnotation(), jp);
            }
            return jp.proceed(jp.getArgs());
        } finally {
            // 释放threadLocal的对象
            Info.unset();
        }

    }

}
