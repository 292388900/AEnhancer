package com.baidu.aenhancer.core.processor.ext.impl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.impl.AsyncSplitNTimeoutProcessor;
import com.baidu.aenhancer.core.processor.impl.CacheProcessor;
import com.baidu.aenhancer.core.processor.impl.FallBackProcessor;
import com.baidu.aenhancer.core.processor.impl.HookProcessor;
import com.baidu.aenhancer.core.processor.impl.PlainInvokProcessor;
import com.baidu.aenhancer.core.processor.impl.RetryProcessor;
import com.baidu.aenhancer.core.processor.impl.ShortCircuitProcessor;
import com.baidu.aenhancer.core.processor.impl.SyncSplitProcessor;
import com.baidu.aenhancer.core.processor.impl.SyncTimeoutProcessor;

/**
 * builder 模式，将拓扑顺序封装在builer中
 * 
 * @author xushuda
 *
 */
@Component
@Scope("prototype")
public class ProcessorBuilder {

    private boolean cache = false;
    private boolean retry = false;
    private boolean split = false;
    private boolean timeout = false;
    private boolean parallel = false;
    private boolean fallback = false;
    private boolean shortcircuit = false;
    private ApplicationContext ctx;

    public ProcessorBuilder isShortcircuit(boolean shortcircuit) {
        this.shortcircuit = shortcircuit;
        return this;
    }

    /**
     * @param fallback
     * @return
     */
    public ProcessorBuilder isFallback(boolean fallback) {
        this.fallback = fallback;
        return this;
    }

    /**
     * @param timeout the timeout to set
     */
    public ProcessorBuilder isTimeout(boolean timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @param aggr the aggr to set
     */
    public ProcessorBuilder isSplit(boolean isSplit) {
        this.split = isSplit;
        return this;
    }

    /**
     * @param batch the batch to set
     */
    public ProcessorBuilder isParallel(boolean isParallel) {
        this.parallel = isParallel;
        return this;
    }

    /**
     * @param cache the cache to set
     */
    public ProcessorBuilder isCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    /**
     * @param retry the retry to set
     */
    public ProcessorBuilder isRetry(boolean retry) {
        this.retry = retry;
        return this;
    }

    /**
     * 描述对象组织的 Topological order
     * 
     * @return
     */
    public Processor build() {
        Processor processor = getBean(PlainInvokProcessor.class);
        // 重试
        if (retry) {
            processor = getBean(RetryProcessor.class).hookee(processor);
        }
        if (parallel) {
            // split and timeout
            if (timeout || split) {
                processor = getBean(AsyncSplitNTimeoutProcessor.class).hookee(processor);
            }
        } else {
            if (timeout) {
                // timeout
                processor = getBean(SyncTimeoutProcessor.class).hookee(processor);
            }
            if (split) {
                // split
                processor = getBean(SyncSplitProcessor.class).hookee(processor);
            }
        }

        // shortcircuit
        if (shortcircuit) {
            processor = getBean(ShortCircuitProcessor.class).hookee(processor);
        }

        // cache
        if (cache) {
            processor = getBean(CacheProcessor.class).hookee(processor);
        }

        // fallback
        if (fallback) {
            processor = getBean(FallBackProcessor.class).hookee(processor);
        }
        
        // hook processor是最先被调用的
        return getBean(HookProcessor.class).hookee(processor);
    }

    // TODO
    private Processor getBean(Class<? extends Processor> clazz) {
        String[] beanNames = ctx.getBeanNamesForType(clazz);
        if (beanNames == null || beanNames.length == 0) {
            throw new NullPointerException(" false xml, no bean found for  " + clazz);
        }
        // 只有一个符合，直接返回
        if (beanNames.length == 1) {
            return ctx.getBean(beanNames[0], clazz);
        }
        // 有多个符合，不使用默认
        for (String beanName : beanNames) {
            if (!beanName.startsWith("__default")) {
                return ctx.getBean(beanName, clazz);
            }
        }
        throw new NullPointerException("should not reach here, or u have false applicationContext.xml configuration "
                + clazz);
    }

    public ProcessorBuilder setCtx(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
        return this;
    }
}
