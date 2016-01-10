package com.baidu.ascheduler.entry;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.baidu.ascheduler.cache.driver.CacheDriverFactory;
import com.baidu.ascheduler.context.Aggregation;
import com.baidu.ascheduler.context.AnnotationInfo;
import com.baidu.ascheduler.context.AopContext;
import com.baidu.ascheduler.context.SignatureInfo;
import com.baidu.ascheduler.exception.IllegalParamException;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.processor.Initd;

/**
 * 定义Cached修饰的切点（point cut）和它的连接点（join point）处理
 * 
 * @author xushuda
 *
 */
@Aspect
public class SchedAspect {

    private static final Logger logger = LoggerFactory.getLogger(SchedAspect.class);

    // 没有用spring托管
    private Initd initd = new Initd();

    @Autowired
    private CacheDriverFactory cacheDriverFactory;

    /**
     * around 入口,这个方法会捕获所有runtimeException（可能由cacheDriver或者框架抛出，比如网络异常） <br>
     * 还有post processor抛出的所有CacheAopException的子类都会被捕获。但是pre processor抛出的异常都是明显编码错误，直接抛出
     * 
     * @param jp
     * @param scheduled
     * @return 方法调用的返回值
     * @throws Throwable
     */
    @Around("@annotation(scheduled)")
    public Object around(ProceedingJoinPoint jp, Sched scheduled) throws Throwable {
        // 预处理，这里抛出的异常是受检异常，应该直接抛出，所以不在try块中
        AopContext ctx = parse(jp, scheduled);
        // 获取driver
        if (!StringUtils.isEmpty(scheduled.cache())) {
            ctx.setCacheDriver(cacheDriverFactory.getCacheDriver(scheduled.cache()));
        }
        // 真正的数据处理阶段
        try {
            logger.info("ctx_id: {} , start to retrive data from: {} ", ctx.getCtxId(), jp.getSignature()
                    .toLongString());
            Object ret = initd.start(ctx);
            logger.info("data got for ctx_id: " + ctx.getCtxId() + " is: " + ret);
            return ret;
        } catch (SchedAopException exp) {
            // be careful, this kind of exception may caused by your incorrect code
            logger.error("ctx_id: {} ,revive error occors in cache aop , caused by :", ctx.getCtxId(), exp);
            if (ctx.getBatchSize() > 0) {
                return initd.startPlainWithBatch(ctx);
            }
            // return the original call
            return jp.proceed(jp.getArgs());
        } catch (RuntimeException rtExp) {
            // swallow the runtime exception
            logger.error("ctx_id: {} , revive runtime exception occurs in cache aop , caused by :", ctx.getCtxId(),
                    rtExp);
            // be careful about this kind of exception, the cache server may just crash
            if (ctx.getBatchSize() > 0) {
                return initd.startPlainWithBatch(ctx);
            }
            return jp.proceed(jp.getArgs());
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
    private AopContext parse(ProceedingJoinPoint jp, Sched cached) throws IllegalParamException {
        // 解析注解
        AnnotationInfo annotation = parseAnnotation(cached);
        // 解析函数签名
        SignatureInfo signature = parseSignature(jp, annotation.aggrInvok());
        // validate the signature and annotation
        validate(signature, annotation);
        // 生成ctx对象
        AopContext ctx = new AopContext(signature, annotation, jp);
        // fail fast,在这之前抛出的异常都是由于编码的错误，所以，不应该捕获
        // log
        logger.info("success getting ctx {}", ctx);
        return ctx;
    }

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
    private AnnotationInfo parseAnnotation(Sched cached) throws IllegalParamException {
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
}
