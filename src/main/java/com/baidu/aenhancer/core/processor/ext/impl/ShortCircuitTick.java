package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.conf.runtime.Configurable;
import com.baidu.aenhancer.conf.runtime.ShortCircuitSlidingWindowConfig;

/**
 * 包含一个Timer线程，TickTock,
 * 
 * 持有所有方法的滑动窗口对象
 * 
 * @author xushuda
 *
 */
public class ShortCircuitTick implements Configurable<ShortCircuitSlidingWindowConfig> {

    private final static Logger logger = LoggerFactory.getLogger(ShortCircuitTick.class);

    private volatile Timer timer = new Timer(true);

    private volatile int tick; // 当前的时间戳id

    private volatile ShortCircuitSlidingWindowConfig config;

    private final ConcurrentHashMap<Method, SlidingWindow<ShortCircuitFigure>> slideMaps;

    public int getTick() {
        return tick;
    }

    /**
     * 
     * @param interval 每个时间间隔的大小
     */
    public ShortCircuitTick() {
        slideMaps = new ConcurrentHashMap<Method, SlidingWindow<ShortCircuitFigure>>();
        config = new ShortCircuitSlidingWindowConfig();
        config(config);
    }

    /**
     * 
     * @param method 方法
     * @param calSize 计算的窗口个数
     * @return
     */
    public int getSuccesss(Method method, int calSize) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return calSuccess(window.get(tick, calSize));
    }

    public int getTimeouts(Method method, int calSize) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return calTimeout(window.get(tick, calSize));
    }

    public int getRejections(Method method, int calSize) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return calReject(window.get(tick, calSize));
    }

    public int getErrors(Method method, int calSize) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return calError(window.get(tick, calSize));
    }

    public int success(Method method) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return window.get(tick).successs.addAndGet(1);
    }

    public int reject(Method method) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return window.get(tick).rejections.addAndGet(1);
    }

    public int error(Method method) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return window.get(tick).errors.addAndGet(1);
    }

    public int timeout(Method method) {
        SlidingWindow<ShortCircuitFigure> window = getSlideWindow(method);
        return window.get(tick).timeouts.addAndGet(1);
    }

    private int calSuccess(List<ShortCircuitFigure> figures) {
        int success = 0;
        for (ShortCircuitFigure figure : figures) {
            success += figure.successs.get();
        }
        return success;
    }

    private int calError(List<ShortCircuitFigure> figures) {
        int fail = 0;
        for (ShortCircuitFigure figure : figures) {
            fail += figure.errors.get();
        }
        return fail;
    }

    private int calTimeout(List<ShortCircuitFigure> figures) {
        int timeout = 0;
        for (ShortCircuitFigure figure : figures) {
            timeout += figure.timeouts.get();
        }
        return timeout;
    }

    private int calReject(List<ShortCircuitFigure> figures) {
        int rej = 0;
        for (ShortCircuitFigure figure : figures) {
            rej += figure.rejections.get();
        }
        return rej;
    }

    /**
     * 在get window的时候会确定window的大小
     * 
     * @param method
     * @return
     */
    private SlidingWindow<ShortCircuitFigure> getSlideWindow(Method method) {
        if (null == method) {
            throw new NullPointerException("method is null");
        }
        SlidingWindow<ShortCircuitFigure> window = slideMaps.get(method);
        if (null == window) {
            ShortCircuitFigure[] scf = new ShortCircuitFigure[config.getWindowSize()];
            for (int i = 0; i < config.getWindowSize(); i++) {
                scf[i] = new ShortCircuitFigure();
            }
            window = new SlidingWindow<ShortCircuitFigure>(scf);
            slideMaps.putIfAbsent(method, window);
        }
        return slideMaps.get(method);
    }

    /**
     * 窗口内每个slot中存的对象
     * 
     * @author xushuda
     *
     */
    class ShortCircuitFigure {
        AtomicInteger successs = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger timeouts = new AtomicInteger(0);
        AtomicInteger rejections = new AtomicInteger(0);

        // 重置
        public void reset() {
            successs.set(0);
            errors.set(0);
            timeouts.set(0);
            rejections.set(0);
        }
    }

    /**
     * 包含一个固定大小的T的数组
     * 
     * @author xushuda
     *
     */
    class SlidingWindow<T> {

        /**
         * 滑动窗口
         */
        private final T[] slots;

        /**
         * 窗口大小
         * 
         * @return
         */
        public int getWindowSize() {
            return slots.length;
        }

        /**
         * 
         * @param windowSize 窗口的大小
         */
        public SlidingWindow(T[] arrays) {
            slots = arrays;
        }

        public T get(int id) {
            return slots[id & 0x7FFFFFF % slots.length];
        }

        public List<T> get(int id, int size) {
            List<T> ret = new LinkedList<T>();
            for (int i = 0; i < size; i++) {
                ret.add(get(id - i));
            }
            return ret;
        }

        public void set(int id, T data) {
            slots[id & 0x7FFFFFF % slots.length] = data;
        }
    }

    @Override
    public ShortCircuitSlidingWindowConfig getConfig() {
        return config;
    }

    /**
     * 线程安全，对该对象进行配置
     */
    @Override
    public synchronized void config(final ShortCircuitSlidingWindowConfig config) {
        ShortCircuitSlidingWindowConfig oldConfig = this.config;
        this.config = config; // 这行代码之后，新的window的size就是新的config中的大小了
        // 原有windowSize，新windowSize不一样
        if (null != oldConfig && oldConfig.getWindowSize() != config.getWindowSize()) {
            for (Entry<Method, SlidingWindow<ShortCircuitFigure>> entry : slideMaps.entrySet()) {
                int tmpTick = this.tick;
                // 可能遍历到新的window
                if (config.getWindowSize() == oldConfig.getWindowSize()) {
                    continue;
                }
                int minSize = Math.min(config.getWindowSize(), oldConfig.getWindowSize());
                // 新的slidingWindow
                SlidingWindow<ShortCircuitFigure> toSw =
                        new SlidingWindow<ShortCircuitFigure>(new ShortCircuitFigure[config.getWindowSize()]);
                // 从原有的slidingWindow拷贝数据
                SlidingWindow<ShortCircuitFigure> fromSw = entry.getValue();
                for (int i = minSize; i >= 0; i--) {
                    toSw.set(tmpTick - i, fromSw.get(tmpTick - i));
                }
                // 这时候修改了window的size,新的数据会写入新的slidingWindow但是timer的时间间隔还是没有修改
                entry.setValue(toSw);
            }
        }
        if (null != timer) {
            timer.cancel();
        }
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int newTick = tick + 1;
                // 删除已有旧数据，没有的话为空操作。TODO 这里可能有多个线程运行这段代码，线性的速度下降风险
                for (SlidingWindow<ShortCircuitFigure> sw : slideMaps.values()) {
                    sw.get(newTick).reset();
                }
                // 清除newTick中旧的数据后，新的数据会写入新的block中
                tick = newTick; // 每过interval时间就会+1
                logger.info("after {} milliseconds, ++tick={}", config.getInterval(), tick);
            }
        }, config.getInterval(), config.getInterval());
    }

    @Override
    public String namespace() {
        return "shortcircuit";
    }
}
