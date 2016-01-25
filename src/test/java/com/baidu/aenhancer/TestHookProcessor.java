package com.baidu.aenhancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.impl.HookProcessor;

@Component
@Scope("prototype")
public class TestHookProcessor extends HookProcessor {
    private Logger logger = LoggerFactory.getLogger(TestHookProcessor.class);

    protected void preCheck(ProcessContext ctx, Object param) {
        logger.info("override preCheck in user's hook");
    }

}
