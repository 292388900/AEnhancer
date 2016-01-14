package com.baidu.aenhancer;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.Splitable;
import com.baidu.aenhancer.entry.Collapse;
import com.baidu.aenhancer.entry.Split;
import com.baidu.aenhancer.exception.CodingError;

public class CustomSpliter implements Splitable {

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {

    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {

    }

    @Split
    public List<Object[]> kill(int start, int end) {
        List<Object[]> ret = new ArrayList<Object[]>();
        ret.add(new Object[] { start, start });
        ret.add(new Object[] { end, end });
        return ret;
    }

    @Collapse
    public Integer collapse(List<Integer> result) {
        int x = 0;
        for (Integer i : result) {
            x += i;
        }
        return x;
    }

}
