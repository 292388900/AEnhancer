package com.baidu.aenhancer.core.context;

import java.lang.reflect.Method;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.processor.DecoratableProcessor;
import com.baidu.aenhancer.core.processor.ExecutorFactory;
import com.baidu.aenhancer.core.processor.ext.Cacheable;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
import com.baidu.aenhancer.core.processor.ext.Fallbackable;
import com.baidu.aenhancer.core.processor.ext.Splitable;
import com.baidu.aenhancer.entry.Enhancer;
import com.baidu.aenhancer.entry.FallbackMock;
import com.baidu.aenhancer.exception.CodingError;

/**
 * 处理上下文的AOP版本（从annotation中获取的信息）
 * 
 * hold variable to provide info of signature and annotation
 * 
 * @author xushuda
 *
 */
public class AopContext implements ProcessContext {

    private final long ctxId;
    private Enhancer annotation;
    private final Object[] clonedArgs; // 克隆的原参数
    private ProceedingJoinPoint jp; // join point
    // 子流程
    private Cacheable cacher = null;
    private Splitable spliter = null;
    private FallbackProxy fallback = null;

    public AopContext(Enhancer annotation, ProceedingJoinPoint jp, ApplicationContext context)
            throws InstantiationException, IllegalAccessException, CodingError {
        ctxId = new Random().nextLong();
        this.annotation = annotation;
        this.jp = jp;
        clonedArgs = jp.getArgs().clone();

        // cacher
        if (annotation.cacher() != Enhancer.NULL.class) {
            cacher = annotation.cacher().newInstance();
            cacher.init(jp, context);
        }

        // spliter
        if (annotation.spliter() != Enhancer.NULL.class) {
            spliter = annotation.spliter().newInstance();
            spliter.init(jp, context);
        }

        // fall back
        if (annotation.fallback() != Enhancer.NULL.class) {
            fallback = genInnerFallBack(annotation.fallback().newInstance());
            if (null == fallback) {
                throw new CodingError("no @FallBackMock on any method of class:" + annotation.fallback());
            }
            fallback.init(jp, context);
        }
    }

    private FallbackProxy genInnerFallBack(final Fallbackable userFallback) {
        if (null == userFallback) {
            return null;
        }
        for (final Method method : userFallback.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(FallbackMock.class)) {
                return new FallbackProxy() {
                    @Override
                    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
                        userFallback.init(jp, context);
                    }

                    @Override
                    public void beforeProcess(ProcessContext ctx, DecoratableProcessor currentProcess) {
                        userFallback.beforeProcess(ctx, currentProcess);
                    }

                    @SuppressWarnings("finally")
                    @Override
                    public Object fallback(Object[] param) {
                        try {
                            return method.invoke(userFallback, param);
                        } finally {
                            return null;
                        }
                    }
                };
            }
        }
        return null;
    }

    /**
     * 获取克隆的原始参数的引用，所以，直接修改这个数组的的内容不会对原对象产生影响<br>
     * 但是是不能修改数组中引用的对象
     * 
     * @return cloned的参数数组
     */
    @Override
    public Object[] getArgs() {
        return clonedArgs;
    }

    /**
     * 
     * @param args 参数
     * @return 返回按照给入参数调用原方法的结果
     * @throws Throwable 异常
     */
    @Override
    public Object invokeOrignialMethod(Object[] args) throws Throwable {
        return jp.proceed(args);
    }

    @Override
    public int getRetry() {
        return annotation.retry();
    }

    @Override
    public long getCtxId() {
        return ctxId;
    }

    @Override
    public int getTimeout() {
        return annotation.timeout();
    }

    @Override
    public Cacheable getCacher() {
        if (null == cacher) {
            throw new NullPointerException("cacher is null");
        }
        return cacher;
    }

    @Override
    public Splitable getSpliter() {
        if (null == spliter) {
            throw new NullPointerException("spliter is null");
        }
        return spliter;
    }

    @Override
    public boolean split() {
        return spliter != null;
    }

    @Override
    public boolean cache() {
        return cacher != null;
    }

    @Override
    public boolean parallel() {
        return annotation.parallel();
    }

    @Override
    public boolean fallback() {
        return fallback != null;
    }

    @Override
    public FallbackProxy getFallBack() {
        if (null == fallback) {
            throw new NullPointerException("fallback is null");
        }
        return fallback;
    }

    @Override
    public String getGroup() {
        String group = annotation.group();
        return StringUtils.isEmpty(group) ? ExecutorFactory.SHARED : group;
    }
}
