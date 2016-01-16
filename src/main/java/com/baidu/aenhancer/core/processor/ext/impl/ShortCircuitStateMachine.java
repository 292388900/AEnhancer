package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 只支持全局设置 TODO
 * 
 * @author xushuda
 *
 */
public class ShortCircuitStateMachine {

    private final static Logger logger = LoggerFactory.getLogger(ShortCircuitStateMachine.class);

    // 状态的操作要原子，通路，中间状态，短路
    enum CircuitStatus {
        NATURAL_STATE, MIDDLE_STATE, SHORT_CIRCUIT
    }

    class TimestampdStatus {
        private final CircuitStatus status;
        private final int tick;
        private AtomicInteger trails; // 只有middle state需要
        private AtomicInteger success;
        private AtomicInteger fails;

        public TimestampdStatus(int tick, CircuitStatus status) {
            this.status = status;
            this.tick = tick;
        }
    }

    private static volatile ShortCircuitStateMachine instance;

    public static ShortCircuitStateMachine getInstance() {
        if (null == instance) {
            synchronized (ShortCircuitStateMachine.class) {
                if (null == instance) {
                    instance = new ShortCircuitStateMachine(10, 0.85, 4, 50, 0, 1);
                }
            }
        }
        return instance;
    }

    private ConcurrentHashMap<Method, AtomicReference<TimestampdStatus>> statusMap;

    private int leastSample; // 最小样本个数

    private double minSuccessPerentage; // 失败的比率

    private int elapse; // 自动探索的过程时间

    private int maxTraffic; // 限制流量大小

    private int failLimit; // 至少要成功limit次

    private int successLimit; // 至多失败limit次

    private ShortCircuitStateMachine(int leastSample, double minSuccessPerentage, int elapse, int maxTraffic,
            int failLimit, int successLimit) {
        this.leastSample = leastSample;
        this.minSuccessPerentage = minSuccessPerentage;
        this.elapse = elapse;
        this.maxTraffic = maxTraffic;
        this.failLimit = failLimit;
        this.successLimit = successLimit;
        this.statusMap = new ConcurrentHashMap<Method, AtomicReference<TimestampdStatus>>();
    }

    /**
     * 获取状态，如果没有则创建
     * 
     * @param method
     * @param tick
     * @return
     */
    private AtomicReference<TimestampdStatus> getStatus(Method method, int tick) {
        if (method == null) {
            throw new NullPointerException("method is null");
        }
        AtomicReference<TimestampdStatus> statusRef = statusMap.get(method);
        if (null == statusRef) {
            synchronized (method) {
                if (null == statusRef) {
                    statusRef =
                            new AtomicReference<TimestampdStatus>(new TimestampdStatus(tick,
                                    CircuitStatus.NATURAL_STATE));
                }
            }
        }
        return statusRef;
    }

    /**
     * 
     * @param method
     * @return
     */
    public boolean shortcircuit(Method method, int success, int timeout, int error, int rej, int tick) {
        AtomicReference<TimestampdStatus> tsRef = getStatus(method, tick);
        boolean shortcircuit = false;
        TimestampdStatus ts = tsRef.get();
        switch (ts.status) {
            case NATURAL_STATE:
                if (success + timeout + error + rej > leastSample
                        && (double) success / (timeout + error + rej + success) < minSuccessPerentage) {
                    // 修改状态
                    if (tsRef.compareAndSet(ts, new TimestampdStatus(tick, CircuitStatus.SHORT_CIRCUIT))) {
                        logger.info("success percentage is too low at: \"{}\" , cause short circuit", (double) success
                                / (timeout + error + rej + success));
                    }
                    shortcircuit = true;
                }
                // 流量控制不改变状态
                if (success + timeout + error + rej >= maxTraffic) {
                    shortcircuit = true;
                }
                break;
            case MIDDLE_STATE:
                // 半开放，超过了trails次数就要短路
                if (ts.trails.addAndGet(1) > failLimit + successLimit) {
                    ts.trails.addAndGet(-1);
                    shortcircuit = true;
                }
                break;
            case SHORT_CIRCUIT:
                // 超过elapse时间则变为中间状态
                if (tick > ts.tick + elapse) {
                    TimestampdStatus newTs = new TimestampdStatus(tick, CircuitStatus.MIDDLE_STATE);
                    if (tsRef.compareAndSet(ts, newTs)) {
                        newTs.trails = new AtomicInteger(0);
                        newTs.success = new AtomicInteger(0);
                        logger.info("short circuit start at {} change to middle state at {}", ts.tick, tick);
                    }
                }
                shortcircuit = true;
                break;
        }
        return shortcircuit;
    }

    /**
     * 其实只会在中间状态才会处理
     * 
     * @param isSuccess
     */
    public void notify(Method method, boolean isSuccess, int tick) {
        AtomicReference<TimestampdStatus> tsRef = getStatus(method, tick);
        TimestampdStatus ts = tsRef.get();
        switch (ts.status) {
            case MIDDLE_STATE:
                // 任何一个线程先成功改变状态，别的线程就算已经进入了这个方法区域，也无法成功改变状态
                if (isSuccess) {
                    // 成功
                    if (ts.success.addAndGet(1) >= successLimit) {
                        tsRef.compareAndSet(ts, new TimestampdStatus(tick, CircuitStatus.NATURAL_STATE));
                    }
                    logger.info("success {} in middle state ", ts.success.get());
                } else {
                    // 失败
                    if (ts.fails.addAndGet(1) >= failLimit) {
                        tsRef.compareAndSet(ts, new TimestampdStatus(tick, CircuitStatus.SHORT_CIRCUIT));
                    }
                }

                break;
            default:
                break;
        }
    }
}
