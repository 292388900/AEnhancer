package com.baidu.aenhancer.core.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.CacheProxy;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
import com.baidu.aenhancer.core.processor.ext.Fallbackable;
import com.baidu.aenhancer.core.processor.ext.HookProxy;
import com.baidu.aenhancer.core.processor.ext.Hookable;
import com.baidu.aenhancer.core.processor.ext.ShortCircuitable;
import com.baidu.aenhancer.core.processor.ext.SplitProxy;
import com.baidu.aenhancer.core.processor.ext.Splitable;
import com.baidu.aenhancer.core.processor.ext.impl.ExecPool;
import com.baidu.aenhancer.entry.Collapse;
import com.baidu.aenhancer.entry.Enhancer;
import com.baidu.aenhancer.entry.Enhancer.NULL;
import com.baidu.aenhancer.entry.FallbackMock;
import com.baidu.aenhancer.entry.Hook;
import com.baidu.aenhancer.entry.Split;
import com.baidu.aenhancer.exception.CodingError;
import com.baidu.aenhancer.exception.UnexpectedStateException;

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
    private CacheProxy cacher = null;
    private SplitProxy spliter = null;
    private FallbackProxy fallback = null;
    private HookProxy hook = null;
    private ShortCircuitable shortcircuit = null;

    public AopContext(Enhancer annotation, ProceedingJoinPoint jp, ApplicationContext context)
            throws InstantiationException, IllegalAccessException, CodingError, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        ctxId = new Random().nextLong();
        this.annotation = annotation;
        this.jp = jp;
        clonedArgs = jp.getArgs().clone();

        // cacher
        if (annotation.cacher() != Enhancer.NULL.class) {
            Constructor<? extends CacheProxy> cons = annotation.cacher().getConstructor();
            cons.setAccessible(true);
            cacher = cons.newInstance();
            cacher.init(jp, context);
        }

        // spliter
        if (annotation.spliter() != Enhancer.NULL.class) {
            Constructor<? extends Splitable> cons = annotation.spliter().getConstructor();
            cons.setAccessible(true);
            spliter = getSplitProxy(cons.newInstance());
            if (null == spliter) {
                throw new CodingError("no @Split and @Collapse Annotationed method in class: " + annotation.spliter());
            }
            spliter.init(jp, context);
        }

        // fall back
        if (annotation.fallback() != Enhancer.NULL.class) {
            Constructor<? extends Fallbackable> cons = annotation.fallback().getConstructor();
            cons.setAccessible(true);
            fallback = genInnerFallBack(cons.newInstance());
            if (null == fallback) {
                throw new CodingError("no @FallBackMock on any method of class:" + annotation.fallback());
            }
            fallback.init(jp, context);
        }

        // shortcircuit
        if (annotation.shortcircuit() != NULL.class) {
            Constructor<? extends ShortCircuitable> cons = annotation.shortcircuit().getConstructor();
            cons.setAccessible(true);
            shortcircuit = cons.newInstance();
            shortcircuit.init(jp, context);
        }

        // hook
        if (annotation.hook() == null) {
            throw new CodingError("hook is a must have (not null) annotation, by default is Hooker.class");
        }
        Constructor<? extends Hookable> cons = annotation.hook().getConstructor();
        cons.setAccessible(true);
        hook = getHookProxy(cons.newInstance());
        if (null == hook) {
            throw new CodingError("must have a @Hook method in class: " + annotation.hook());
        }
        hook.init(jp, context);

    }

    private HookProxy getHookProxy(final Hookable userHook) {
        if (null == userHook) {
            return null;
        }
        // 本身就是Proxy类
        if (userHook instanceof HookProxy) {
            return HookProxy.class.cast(userHook);
        }
        // 根据annotation代理到Proxy上
        for (final Method method : userHook.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Hook.class)) {
                return new HookProxy() {

                    @Override
                    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {

                    }

                    @Override
                    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {

                    }

                    @Override
                    public Object call(Object[] param) {
                        try {
                            return method.invoke(userHook, param);
                        } catch (Exception e) {
                            throw new UnexpectedStateException(e);
                        }
                    }
                };
            }
        }
        return null;
    }

    private SplitProxy getSplitProxy(final Splitable userSpliter) throws CodingError {
        if (null == userSpliter) {
            return null;
        }
        if (userSpliter instanceof SplitProxy) {
            return SplitProxy.class.cast(userSpliter);
        }
        Method split = null;
        Method collapse = null;
        for (Method method : userSpliter.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Split.class)) {
                // 必须返回一个List的子类
                if (!List.class.isAssignableFrom(method.getReturnType())) {
                    throw new CodingError("spliter return type must be sub Class of List");
                }
                split = method;
            } else if (method.isAnnotationPresent(Collapse.class)) {
                Class<?>[] paramTypes = method.getParameterTypes();
                // 实际上要把List<Object>传给这个方法，paramTypes[0].isAssignableFrom(List.class)
                if (paramTypes.length != 1 || !paramTypes[0].isAssignableFrom(List.class)) {
                    throw new CodingError("param of collapse must be a list");
                }
                collapse = method;
            }
            // 创建代理类
            if (split != null && collapse != null) {
                final Method fsplier = split;
                final Method fcollapse = collapse;
                return new SplitProxy() {

                    @Override
                    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
                        userSpliter.init(jp, context);

                    }

                    @Override
                    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
                        userSpliter.beforeProcess(ctx, currentProcess);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public List<Object[]> split(Object[] args) {
                        try {
                            return (List<Object[]>) fsplier.invoke(userSpliter, args);
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    public Object collapse(List<Object> result) {
                        try {
                            return fcollapse.invoke(userSpliter, result);
                        } catch (Exception e) {
                            throw new UnexpectedStateException(e);
                        }
                    }
                };
            }
        }
        return null;
    }

    private FallbackProxy genInnerFallBack(final Fallbackable userFallback) {
        if (null == userFallback) {
            return null;
        }
        // 本身就是Proxy类
        if (userFallback instanceof FallbackProxy) {
            return FallbackProxy.class.cast(userFallback);
        }
        // 根据annotation代理到Proxy上
        for (final Method method : userFallback.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(FallbackMock.class)) {
                return new FallbackProxy() {
                    @Override
                    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
                        userFallback.init(jp, context);
                    }

                    @Override
                    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
                        userFallback.beforeProcess(ctx, currentProcess);
                    }

                    @Override
                    public Object fallback(Object[] param) {
                        try {
                            return method.invoke(userFallback, (Object) param);
                        } catch (Exception e) {
                            throw new UnexpectedStateException(e);
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
        return StringUtils.isEmpty(group) ? ExecPool.SHARED : group;
    }

    @Override
    public HookProxy getHook() {
        if (null == hook) {
            throw new NullPointerException("hook is null");
        }
        return hook;
    }

    @Override
    public boolean hook() {
        return null != hook;
    }

    @Override
    public boolean shortcircuit() {
        return shortcircuit != null;
    }

    @Override
    public ShortCircuitable getShortCircuit() {
        if (null == shortcircuit) {
            throw new NullPointerException("shortcircuit is null");
        }
        return shortcircuit;
    }
}
