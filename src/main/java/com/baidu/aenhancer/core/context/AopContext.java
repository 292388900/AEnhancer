package com.baidu.aenhancer.core.context;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.processor.ext.CacheProxy;
import com.baidu.aenhancer.core.processor.ext.Contextural;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
import com.baidu.aenhancer.core.processor.ext.ShortCircuitProxy;
import com.baidu.aenhancer.core.processor.ext.SplitProxy;
import com.baidu.aenhancer.core.processor.ext.impl.ExecPool;
import com.baidu.aenhancer.entry.Enhancer;
import com.baidu.aenhancer.exception.CodingError;
import com.baidu.aenhancer.exception.IllegalParamException;

/**
 * 处理上下文的AOP版本（从annotation中获取的信息）
 * 
 * hold variable to provide info of signature and annotation
 * 
 * @author xushuda
 *
 */
// TODO IOC注入，使得用户可以继承修改，或者提供其他的注册对象获取对象到上线文的方法，（相关修改：builder）
public class AopContext implements ProcessContext {

    private final long ctxId;
    private Enhancer annotation;
    private final Object[] clonedArgs; // 克隆的原参数
    private ProceedingJoinPoint jp; // join point
    // 子流程
    private CacheProxy cacher = null;
    private SplitProxy spliter = null;
    private FallbackProxy fallback = null;
    private ShortCircuitProxy shortcircuit = null;

    private Map<Class<? extends Contextural>, Contextural> extMap;

    public AopContext(Enhancer annotation, ProceedingJoinPoint jp, ApplicationContext context)
            throws InstantiationException, IllegalAccessException, CodingError, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        ctxId = new Random().nextLong();
        this.annotation = annotation;
        this.jp = jp;
        clonedArgs = jp.getArgs().clone();
        // String methodName = ((MethodSignature) jp.getSignature()).getName();

        // new split
        if (!StringUtils.isEmpty(annotation.parallel().spliter())) {
            spliter = context.getBean(annotation.parallel().spliter(), SplitProxy.class);
            spliter.init(jp, context);
        }

        // cacher
        if (!StringUtils.isEmpty(annotation.cacher())) {
            cacher = context.getBean(annotation.cacher(), CacheProxy.class);
            cacher.init(jp, context);
        }

        // fall back
        if (!StringUtils.isEmpty(annotation.fallback())) {
            fallback = context.getBean(annotation.fallback(), FallbackProxy.class);
            fallback.init(jp, context);
        }

        // shortcircuit
        if (!StringUtils.isEmpty(annotation.shortcircuit())) {
            shortcircuit = context.getBean(annotation.shortcircuit(), ShortCircuitProxy.class);
            shortcircuit.init(jp, context);
        }

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
    public CacheProxy getCacher() {
        if (null == cacher) {
            throw new NullPointerException("cacher is null");
        }
        return cacher;
    }

    @Override
    public SplitProxy getSpliter() {
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
        return spliter != null;
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
        String group = annotation.parallel().group();
        return StringUtils.isEmpty(group) ? ExecPool.SHARED : group;
    }

    @Override
    public boolean shortcircuit() {
        return shortcircuit != null;
    }

    @Override
    public ShortCircuitProxy getShortCircuit() {
        if (null == shortcircuit) {
            throw new NullPointerException("shortcircuit is null");
        }
        return shortcircuit;
    }

    public <T extends Contextural> T getExt(Class<T> extClass) {
        Contextural ext = extMap.get(extClass);
        if (ext != null && extClass.isAssignableFrom(ext.getClass())) {
            return extClass.cast(ext);
        }
        throw new IllegalParamException("the instance doesn't exists or class " + extClass);
    }
}
