package com.baidu.aenhancer.core.processor;

import com.baidu.aenhancer.core.processor.impl.AsyncSplitNTimeoutProcessor;
import com.baidu.aenhancer.core.processor.impl.CacheProcessor;
import com.baidu.aenhancer.core.processor.impl.FallBackProcessor;
import com.baidu.aenhancer.core.processor.impl.PlainInvokProcessor;
import com.baidu.aenhancer.core.processor.impl.RetryProcessor;
import com.baidu.aenhancer.core.processor.impl.SyncSplitProcessor;
import com.baidu.aenhancer.core.processor.impl.SyncTimeoutProcessor;

/**
 * builder 模式，将拓扑顺序封装在builer中
 * 
 * @author xushuda
 *
 */
public class ProcessorBuilder {

    private boolean cache = false;
    private boolean retry = false;
    private boolean split = false;
    private boolean timeout = false;
    private boolean parallel = false;
    private boolean fallback = false;

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
    public DecoratableProcessor build() {
        DecoratableProcessor processor = new PlainInvokProcessor();
        // 重试
        if (retry) {
            processor = new RetryProcessor().decorate(processor);
        }
        if (parallel) {
            // split and timeout
            if (timeout || split) {
                processor = new AsyncSplitNTimeoutProcessor().decorate(processor);
            }
        } else {
            if (timeout) {
                // timeout
                processor = new SyncTimeoutProcessor().decorate(processor);
            }
            if (split) {
                // split
                processor = new SyncSplitProcessor().decorate(processor);
            }
        }

        // cache
        if (cache) {
            processor = new CacheProcessor().decorate(processor);
        }
        
        // fallback
        if (fallback) {
            processor = new FallBackProcessor().decorate(processor);
        }
        return processor;
    }
}
