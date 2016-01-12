package com.baidu.ascheduler.exec.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exec.DecoratableProcessor;
import com.baidu.ascheduler.ext.Cacheable;

public class CacheProcessor extends DecoratableProcessor {

    private final static Logger logger = LoggerFactory.getLogger(CacheProcessor.class);

    @Override
    protected Object process(ProcessContext ctx, Object param) throws Throwable {
        // get cache
        Cacheable cache = ctx.getCacher();
        // init
        cache.beforeProcess(ctx, this);
        Object cachedData = cache.retrieveFromCache((Object[]) param);
        // 成功获取，则直接返回
        if (cache.shortcircuit()) {
            logger.info("data cached and return directly cached: {}", cachedData);
            return cachedData;
        }
        // 获取新的参数（比如上一步中部分命中）
        Object[] nxtParam = cache.beforeNxtProc();
        // 运行下一个process
        Object ret = decoratee.doo(ctx, nxtParam);
        // 返回结果（可能是合并过的)
        return cache.afterNxtProc(ret);
    }

    @Override
    public void preCheck(ProcessContext ctx, Object param) throws SchedAopException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) throws SchedAopException {
        // TODO Auto-generated method stub
        
    }

}
