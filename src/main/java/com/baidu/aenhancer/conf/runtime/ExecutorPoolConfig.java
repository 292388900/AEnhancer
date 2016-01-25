package com.baidu.aenhancer.conf.runtime;

public class ExecutorPoolConfig implements Cloneable {
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;
    private int queueSize;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public ExecutorPoolConfig(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueSize) {
        super();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueSize = queueSize;
    }

    public ExecutorPoolConfig() {
        this(5, 15, 5000, 20);
    }

}
