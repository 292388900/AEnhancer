package com.baidu.aenhancer.conf.runtime;

public class ShortCircuitStateMachineConfig implements Cloneable {
    private int leastSample; // 最小样本个数

    private double minSuccessPerentage; // 失败的比率

    private int elapse; // 自动探索的过程时间

    private int maxTraffic; // 限制流量大小,小于0代表不限制

    private int failLimit; // 至少要成功limit次,小于0代表不限制

    private int successLimit; // 至多失败limit次,小于0代表不限制

    private int aggregationSize; // 在计算的时候，每次聚合的大小

    public ShortCircuitStateMachineConfig(int leastSample, double minSuccessPerentage, int elapse, int maxTraffic,
            int failLimit, int successLimit, int aggregationSize) {
        super();
        this.leastSample = leastSample;
        this.minSuccessPerentage = minSuccessPerentage;
        this.elapse = elapse;
        this.maxTraffic = maxTraffic;
        this.failLimit = failLimit;
        this.successLimit = successLimit;
        this.aggregationSize = aggregationSize;
    }

    public ShortCircuitStateMachineConfig() {
        this(10, 0.85, 4, -1, 0, 1, 2);
    }

    public int getAggregationSize() {
        return aggregationSize;
    }

    public int getLeastSample() {
        return leastSample;
    }

    public double getMinSuccessPerentage() {
        return minSuccessPerentage;
    }

    public int getElapse() {
        return elapse;
    }

    public int getMaxTraffic() {
        return maxTraffic;
    }

    public int getFailLimit() {
        return failLimit;
    }

    public int getSuccessLimit() {
        return successLimit;
    }
}