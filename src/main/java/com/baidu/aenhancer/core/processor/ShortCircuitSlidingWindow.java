package com.baidu.aenhancer.core.processor;

import java.util.concurrent.atomic.AtomicInteger;

public class ShortCircuitSlidingWindow {

    class ShortCircuit {
        AtomicInteger successs = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger timeouts = new AtomicInteger(0);
        AtomicInteger rejections = new AtomicInteger(0);
    }

    private final ShortCircuit[] slides;

    public ShortCircuitSlidingWindow(int windowSize) {
        slides = new ShortCircuit[windowSize];
    }

    public int getSuccesss(int id) {
        return slides[id % slides.length].successs.get();
    }

    public int getErrors(int id) {
        return slides[id % slides.length].errors.get();
    }

    public int getTimeouts(int id) {
        return slides[id % slides.length].timeouts.get();
    }

    public int getRejctions(int id) {
        return slides[id % slides.length].rejections.get();
    }

    public int success(int id) {
        return slides[id % slides.length].successs.addAndGet(1);
    }

    public int error(int id) {
        return slides[id % slides.length].errors.addAndGet(1);
    }

    public int timeout(int id) {
        return slides[id % slides.length].timeouts.addAndGet(1);
    }

    public int reject(int id) {
        return slides[id % slides.length].rejections.addAndGet(1);
    }

    public int getSuccesss(int id, int size) {
        int ret = 0;
        for (int i = 0; i < size; i++) {
            ret += slides[(id - i) % slides.length].successs.get();
        }
        return ret;
    }

    public int getErrors(int id, int size) {
        int ret = 0;
        for (int i = 0; i < size; i++) {
            ret += slides[(id - i) % slides.length].errors.get();
        }
        return ret;
    }

    public int getTimeouts(int id, int size) {
        int ret = 0;
        for (int i = 0; i < size; i++) {
            ret += slides[(id - i) % slides.length].timeouts.get();
        }
        return ret;
    }

    public int getRejctions(int id, int size) {
        int ret = 0;
        for (int i = 0; i < size; i++) {
            ret += slides[(id - i) % slides.length].rejections.get();
        }
        return ret;
    }
}
