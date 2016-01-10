package com.baidu.ascheduler.processor;

/**
 * builder 模式，将拓扑顺序封装在builer中
 * 
 * @author xushuda
 *
 */
public class ProcessorBuilder {

    private boolean aggr = false; // 参数有集合，需要分批或者单独命中
    private boolean batch = false;
    private boolean cache = false;
    private boolean retry = false;
    private boolean timeout = false;

    /**
     * @param timeout the timeout to set
     */
    public ProcessorBuilder setTimeout(boolean timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @param aggr the aggr to set
     */
    public ProcessorBuilder setAggr(boolean aggr) {
        this.aggr = aggr;
        return this;
    }

    /**
     * @param batch the batch to set
     */
    public ProcessorBuilder setBatch(boolean batch) {
        this.batch = batch;
        return this;
    }

    /**
     * @param cache the cache to set
     */
    public ProcessorBuilder setCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    /**
     * @param retry the retry to set
     */
    public ProcessorBuilder setRetry(boolean retry) {
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
        // 超时时间
        if (timeout) {
            processor = new SyncTimeoutProcessor().decorate(processor);
        }
        // 重试
        if (retry) {
            processor = new RetryProcessor().decorate(processor);
        }
        // 聚合式调用
        if (aggr) {
            // 把一个调用分多批，这个BatchProcessor和AggrCacheProcessor都是基于集合类参数的
            if (batch) {
                processor = new BatchProcessor().decorate(processor);
            }
            if (cache) {
                processor = new AggrCacheProcessor().decorate(processor);
            }
        } else {
            if (cache) {
                processor = new PlainCacheProcessor().decorate(processor);
            }
        }
        return processor;
    }
}
