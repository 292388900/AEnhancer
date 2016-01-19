package com.baidu.aenhancer.core.conf;

/**
 * 
 * @author xushuda
 *
 * @param <T>
 */
public interface Configurable<T> {

    /**
     * 获取现有配置信息
     * 
     * @return
     */
    public T getConfig();

    /**
     * 设置
     * 
     * @param config
     */
    public void config(T config);

    /**
     * 配置的命名空间，同个类的不同对象可以返回不一样的值，以使用不同的配置字段
     * 
     * @return
     */
    public String namespace();

}
