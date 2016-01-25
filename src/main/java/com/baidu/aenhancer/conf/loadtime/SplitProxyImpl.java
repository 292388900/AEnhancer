package com.baidu.aenhancer.conf.loadtime;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.SplitProxy;
import com.baidu.aenhancer.exception.CodingError;

public class SplitProxyImpl implements SplitProxy {

    private StatelessCommand split;
    private StatelessCommand collapse;

    public void setSplit(StatelessCommand split) {
        this.split = split;
    }

    public void setCollapse(StatelessCommand collapse) {
        this.collapse = collapse;
    }

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {

    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {

    }

    @Override
    public Object collapse(List<Object> result) {
        return collapse.call(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object[]> split(Object[] args) {
        return (List<Object[]>) split.call(args);
    }

}
