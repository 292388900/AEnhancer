package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.conf.Configurable;
import com.baidu.aenhancer.core.conf.ShortCircuitStateMachineConfig;
import com.baidu.aenhancer.exception.IllegalParamException;

/**
 * @author xushuda
 *
 */
public class ShortCircuitStateMachine implements Configurable<ShortCircuitStateMachineConfig> {

    private Method method;

    private final AtomicReference<TimestampdStatus> statusRef;

    private volatile ShortCircuitStateMachineConfig config;

    private final static Logger logger = LoggerFactory.getLogger(ShortCircuitStateMachine.class);

    // 状态的操作要原子，通路，中间状态，短路
    enum CircuitStatus {
        NATURAL_STATE, MIDDLE_STATE, SHORT_CIRCUIT
    }

    class TimestampdStatus {
        private final CircuitStatus status;
        private final int tick;
        // 只有middle state需要
        private AtomicInteger trails;
        private AtomicInteger success;
        private AtomicInteger fails;

        public TimestampdStatus(int tick, CircuitStatus status) {
            this.status = status;
            this.tick = tick;
        }
    }

    public ShortCircuitStateMachine(Method method, Integer tick) {
        if (null == method) {
            throw new IllegalParamException("method is null");
        }
        this.method = method;
        this.statusRef = new AtomicReference<TimestampdStatus>(new TimestampdStatus(tick, CircuitStatus.NATURAL_STATE));
        this.config = new ShortCircuitStateMachineConfig();
        config(config);
    }

    /**
     * 
     * @param method
     * @return
     */
    public boolean shortcircuit(int success, int timeout, int error, int rej, int tick) {
        // AtomicReference<TimestampdStatus> tsRef = getStatus(method, tick);
        boolean shortcircuit = false;
        TimestampdStatus ts = statusRef.get();
        switch (ts.status) {
            case NATURAL_STATE:
                if (success + timeout + error + rej > config.getLeastSample()
                        && (double) success / (timeout + error + rej + success) < config.getMinSuccessPerentage()) {
                    // 修改状态
                    if (statusRef.compareAndSet(ts, new TimestampdStatus(tick, CircuitStatus.SHORT_CIRCUIT))) {
                        logger.info(
                                "method : \".{}({})\" success percentage is too low at: \"{}\" , cause short circuit",
                                method.getName(), method.getParameterTypes(), (double) success
                                        / (timeout + error + rej + success));
                    }
                    shortcircuit = true;
                }
                // 流量控制不改变状态
                if (config.getMaxTraffic() >= 0 && success + timeout + error + rej >= config.getMaxTraffic()) {
                    shortcircuit = true;
                }
                break;
            case MIDDLE_STATE:
                // 半开放，超过了trails次数就要短路
                if (ts.trails.addAndGet(1) > config.getFailLimit() + config.getSuccessLimit()) {
                    ts.trails.addAndGet(-1);
                    shortcircuit = true;
                }
                break;
            case SHORT_CIRCUIT:
                // 超过elapse时间则变为中间状态
                if (tick > ts.tick + config.getElapse()) {
                    TimestampdStatus newTs = new TimestampdStatus(tick, CircuitStatus.MIDDLE_STATE);
                    if (statusRef.compareAndSet(ts, newTs)) {
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
    public void notify(boolean isSuccess, int tick) {
        // AtomicReference<TimestampdStatus> tsRef = getStatus(method, tick);
        TimestampdStatus ts = statusRef.get();
        switch (ts.status) {
            case MIDDLE_STATE:
                // 任何一个线程先成功改变状态，别的线程就算已经进入了这个方法区域，也无法成功改变状态
                if (isSuccess) {
                    // 成功
                    if (config.getSuccessLimit() >= 0 && ts.success.addAndGet(1) >= config.getSuccessLimit()) {
                        statusRef.compareAndSet(ts, new TimestampdStatus(tick, CircuitStatus.NATURAL_STATE));
                    }
                    logger.info("success {} in middle state ", ts.success.get());
                } else {
                    // 失败
                    if (config.getFailLimit() >= 0 && ts.fails.addAndGet(1) >= config.getFailLimit()) {
                        statusRef.compareAndSet(ts, new TimestampdStatus(tick, CircuitStatus.SHORT_CIRCUIT));
                    }
                }

                break;
            default:
                break;
        }
    }

    @Override
    public ShortCircuitStateMachineConfig getConfig() {
        return config;
    }

    @Override
    public void config(ShortCircuitStateMachineConfig newConfig) {
        this.config = newConfig;
    }

    @Override
    public String namespace() {
        return method.getDeclaringClass().getName() + "." + method.getName() + ".short.circuit";
    }

    public int getAggregationSize() {
        return config.getAggregationSize();
    }
}
