package com.baidu.aenhancer.core.processor.ext;

import java.util.List;

public interface SplitProxy extends Splitable {
    /**
     * 将多次调用的结果合并
     * 
     * @param result
     * @return
     */
    public Object collapse(List<Object> result);

    /**
     * 拆成多个参数
     * 
     * @param args
     * @return
     */
    public List<Object[]> split(Object[] args);
}
