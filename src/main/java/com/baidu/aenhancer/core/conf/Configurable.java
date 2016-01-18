package com.baidu.aenhancer.core.conf;

public interface Configurable<T> {

    public T getConfig();

    public void config(T config);

    public String namespace();
    
    
}
