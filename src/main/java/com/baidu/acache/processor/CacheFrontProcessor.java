package com.baidu.acache.processor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baidu.acache.entry.Cached;
import com.baidu.acache.exception.CacheAopException;
import com.baidu.acache.exception.IllegalParamException;
import com.baidu.acache.model.Aggregation;
import com.baidu.acache.model.AnnotationInfo;
import com.baidu.acache.model.MethodInfo;
import com.baidu.acache.model.SignatureInfo;

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
     * @return
     */
    private AnnotationInfo parseAnnotation(Cached cached) {
        return new AnnotationInfo(cached.keyInParam(), cached.keyInResult(), cached.batchLimit(), cached.driver(),
                cached.expiration(), cached.ignList(), cached.nameSpace());
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
        if (!anno.validateExt()) {
            throw new IllegalParamException("annotation is error,extK==null and extParam!=null");
        }
        if (!sig.aggrAccessible() && anno.getBatchSize() > 0) {
            throw new IllegalParamException(
                    " error annotation, if a method's signature is not aggregation accessible, can't use batch ");
        }
    }

    /**
     * around 入口,这个方法会捕获所有runtimeException（可能由cacheDriver抛出，比如网络异常） <br>
     * 还有所有CacheAopException的子类，由于这里都会统一捕获，都为受检异常（实际上，某些不希望被捕获的异常，比如代码有问题
     * ：annotation与方法的签名无法匹配，应该为runtimeException，但是这里都统一捕获了，所以不做区分）
     * 
     * @param jp
     * @param cached
     * @return
     * @throws Throwable
     */
    public Object aopAround(ProceedingJoinPoint jp, Cached cached) throws Throwable {
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
        try {
            // 根据不同的请求类型
            if (!annotation.aggrInvok()) {
                logger.info("start to retrive data from {} ", signature.toString());
                return cacheProcessor.processNormal(methodInfo);
            } else {
                return cacheProcessor.processAggregated(methodInfo);
            }
        } catch (CacheAopException exp) {
            // be careful, this kind of exception may caused by your incorrect code
            logger.error("error occors in cache aop , caused by :", exp);
            if (methodInfo.getBatchSize() > 0) {
                return cacheProcessor.processAggregatedWithoutCache(methodInfo);
            }
            // return the original call
            return jp.proceed(jp.getArgs());
        } catch (RuntimeException rtExp) {
            // swallow the runtime exception
            logger.error("runtime exception occurs in cache aop , caused by :", rtExp);
            // be careful about this kind of exception, the cache server may just crash
            if (methodInfo.getBatchSize() > 0) {
                return cacheProcessor.processAggregatedWithoutCache(methodInfo);
            }
            return jp.proceed(jp.getArgs());
        }

    }

}
