package com.baidu.aenhancer.conf.loadtime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
import com.baidu.aenhancer.exception.CodingError;

public class FallbackImpl implements FallbackProxy {

    private StatelessCommand command;

    public void setCommand(StatelessCommand command) {
        this.command = command;
    }

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object fallback(Object[] param) {
        return command.call(param);
    }

}
