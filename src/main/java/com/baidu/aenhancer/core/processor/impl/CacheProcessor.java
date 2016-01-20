package com.baidu.aenhancer.core.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.CacheProxy;
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CacheProcessor extends Processor {

    private final static Logger logger = LoggerFactory.getLogger(CacheProcessor.class);

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        // get cache
        CacheProxy cache = ctx.getCacher();
        // init
        cache.beforeProcess(ctx, this);
        // 参数或者结果
        Object data = cache.retrieveFromCache((Object[]) param);
        // 成功获取，则直接返回
        if (cache.allCached()) {
            logger.info("data cached and return directly cached: {}", data);
            return data;
        }
        // 运行下一个process
        Object ret = decoratee.doo(ctx, data);
        // 返回结果（可能是合并过的)
        return cache.cacheNcollapse(ret);
    }

    @Override
    public void preCheck(ProcessContext ctx, Object param) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) {
        // TODO Auto-generated method stub

    }

}
