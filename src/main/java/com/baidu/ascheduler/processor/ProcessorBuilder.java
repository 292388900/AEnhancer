package com.baidu.ascheduler.processor;

import com.baidu.ascheduler.exec.DecoratableProcessor;

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

        // 重试
        if (retry) {
            processor = new RetryProcessor().decorate(processor);
        }
        // 最先调用的cache
        if (cache) {
            processor = new CacheProcessor().decorate(processor);
        }
        return processor;
    }
}
