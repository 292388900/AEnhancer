package com.baidu.aenhancer.core.processor.ext;


public interface ShortCircuitable extends Contextural {

    public boolean shortcircuit();

    public abstract void success();

    public abstract void error();

    public abstract void timeout();

    public abstract void reject();
}
