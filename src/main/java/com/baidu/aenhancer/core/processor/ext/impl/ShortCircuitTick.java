package com.baidu.aenhancer.core.processor.ext.impl;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 包含一个Timer线程，TickTock
 * 
 * @author xushuda
 *
 */
public class ShortCircuitTick {
    private final static Logger logger = LoggerFactory.getLogger(ShortCircuitTick.class);

    private final Timer timer = new Timer(true);

    private volatile int tick; // 当前的时间戳id

    private final int windowSize; // 时间窗口的大小

    private final ConcurrentHashMap<Method, ShortCircuitSlidingWindow> slideMaps;

    private static volatile ShortCircuitTick instance;

    /**
     * @return
     */
    public static ShortCircuitTick getInstance() {
        if (null == instance) {
            synchronized (ShortCircuitTick.class) {
                if (null == instance) {
                    instance = new ShortCircuitTick(5000, 20);
                }
            }
        }
        // TODO 读配置xml或者文件
        return instance;
    }

    public int getTick() {
        return tick;
    }

    /**
     * 
     * @param interval 每个时间间隔的大小
     */
    private ShortCircuitTick(final long interval, int windowSize) {
        slideMaps = new ConcurrentHashMap<Method, ShortCircuitSlidingWindow>();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tick += 1; // 每过interval时间就会+1
                logger.info("after {} milliseconds, ++tick={}", interval, tick);
            }
        }, interval, interval);
        this.windowSize = windowSize;
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
        if (null == method) {
            throw new NullPointerException("method is null");
        }
        ShortCircuitSlidingWindow window = slideMaps.get(method);
        if (null == window) {
            synchronized (method) {
                if (null == window) {
                    window = new ShortCircuitSlidingWindow(windowSize);
                }
            }
        }
        return window;
    }

    class ShortCircuitSlidingWindow {

        class ShortCircuitFigure {
            AtomicInteger successs = new AtomicInteger(0);
            AtomicInteger errors = new AtomicInteger(0);
            AtomicInteger timeouts = new AtomicInteger(0);
            AtomicInteger rejections = new AtomicInteger(0);
        }

        private final ShortCircuitFigure[] slides;

        public ShortCircuitSlidingWindow(int windowSize) {
            slides = new ShortCircuitFigure[windowSize];
            for (int i = 0; i < windowSize; i++) {
                slides[i] = new ShortCircuitFigure();
            }
        }

        public int getSuccesss(int id) {
            return slides[Math.abs(id) % slides.length].successs.get();
        }

        public int getErrors(int id) {
            return slides[Math.abs(id) % slides.length].errors.get();
        }

        public int getTimeouts(int id) {
            return slides[Math.abs(id) % slides.length].timeouts.get();
        }

        public int getRejctions(int id) {
            return slides[Math.abs(id) % slides.length].rejections.get();
        }

        public int success(int id) {
            return slides[Math.abs(id) % slides.length].successs.addAndGet(1);
        }

        public int error(int id) {
            return slides[Math.abs(id) % slides.length].errors.addAndGet(1);
        }

        public int timeout(int id) {
            return slides[Math.abs(id) % slides.length].timeouts.addAndGet(1);
        }

        public int reject(int id) {
            return slides[Math.abs(id) % slides.length].rejections.addAndGet(1);
        }

        public int getSuccesss(int id, int size) {
            int ret = 0;
            for (int i = 0; i < size; i++) {
                ret += slides[Math.abs((id - i)) % slides.length].successs.get();
            }
            return ret;
        }

        public int getErrors(int id, int size) {
            int ret = 0;
            for (int i = 0; i < size; i++) {
                ret += slides[Math.abs((id - i)) % slides.length].errors.get();
            }
            return ret;
        }

        public int getTimeouts(int id, int size) {
            int ret = 0;
            for (int i = 0; i < size; i++) {
                ret += slides[Math.abs((id - i)) % slides.length].timeouts.get();
            }
            return ret;
        }

        public int getRejctions(int id, int size) {
            int ret = 0;
            for (int i = 0; i < size; i++) {
                ret += slides[Math.abs((id - i)) % slides.length].rejections.get();
            }
            return ret;
        }
    }
}
