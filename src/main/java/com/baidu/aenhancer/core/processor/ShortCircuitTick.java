package com.baidu.aenhancer.core.processor;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ShortCircuitTick {

    enum ShortCircuitStatus {
        CONNECTED_CIRCUIT, SHORT_CIRCUIT
    } // 状态的操作要原子，从短路到通路

    private final AtomicReference<ShortCircuitStatus> status = new AtomicReference<ShortCircuitStatus>(
            ShortCircuitStatus.CONNECTED_CIRCUIT);

    private final Timer timer = new Timer(true);

    private volatile int tick; // 当前的时间戳id

    private final int windowSize; // 时间窗口的大小

    private final int leastSample; // 最小样本个数

    private final double failPercentage; // 失败的比率

    private final int elapse; // 自动探索的过程时间

    private final ConcurrentHashMap<Method, AtomicReference<ShortCircuitSlidingWindow>> slideMaps;

    /**
     * 
     * @return
     */
    public static ShortCircuitTick getInstanc() {
        // TODO 读配置xml或者文件
        return new ShortCircuitTick(5000, 20, 10, 0.75, 4);
    }

    /**
     * 全局配置
     * 
     * @param method
     * @return
     */
    public boolean shortcircuit(Method method) {
        // TODO 状态机
        return false;
    }

    /**
     * 
     * @param interval 每个时间间隔的大小
     */
    private ShortCircuitTick(long interval, int windowSize, int leastSample, double failPercentage, int elapse) {
        slideMaps = new ConcurrentHashMap<Method, AtomicReference<ShortCircuitSlidingWindow>>();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tick += 1; // 每过interval时间就会+1
            }
        }, interval);
        this.windowSize = windowSize;
        this.leastSample = leastSample;
        this.failPercentage = failPercentage;
        this.elapse = elapse;
    }

    /**
     * 
     * @param method 方法
     * @param calSize 计算的窗口个数
     * @return
     */
    public int getSuccesss(Method method, int calSize) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.getSuccesss(tick, calSize);
    }

    public int getTimeouts(Method method, int calSize) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.getTimeouts(tick, calSize);
    }

    public int getRejections(Method method, int calSize) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.getRejctions(tick, calSize);
    }

    public int getErrors(Method method, int calSize) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.getErrors(tick, calSize);
    }

    public int success(Method method) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.success(tick);
    }

    public int reject(Method method) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.reject(tick);
    }

    public int error(Method method) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.error(tick);
    }

    public int timeout(Method method) {
        ShortCircuitSlidingWindow window = getSlideWindow(method);
        return window.timeout(tick);
    }

    private ShortCircuitSlidingWindow getSlideWindow(Method method) {
        AtomicReference<ShortCircuitSlidingWindow> windowRef = slideMaps.get(method);
        windowRef.compareAndSet(null, new ShortCircuitSlidingWindow(windowSize));
        return windowRef.get();
    }
}
