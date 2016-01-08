package com.baidu.ascheduler.entry;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.baidu.ascheduler.cache.driver.CacheDriverFactory;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.model.ProcessContext;

/**
 * 定义Cached修饰的切点（point cut）和它的连接点（join point）处理
 * 
 * @author xushuda
 *
 */
@Aspect
public class SchedAspect {

    private static final Logger logger = LoggerFactory.getLogger(SchedAspect.class);

    // 没有用spring托管
    private Initd initd = new Initd();

    @Autowired
    private CacheDriverFactory cacheDriverFactory;

    /**
     * around 入口,这个方法会捕获所有runtimeException（可能由cacheDriver或者框架抛出，比如网络异常） <br>
     * 还有post processor抛出的所有CacheAopException的子类都会被捕获。但是pre processor抛出的异常都是明显编码错误，直接抛出
     * 
     * @param jp
     * @param scheduled
     * @return 方法调用的返回值
     * @throws Throwable
     */
    @Around("@annotation(scheduled)")
    public Object around(ProceedingJoinPoint jp, Sched scheduled) throws Throwable {
        // 预处理，这里抛出的异常是受检异常，应该直接抛出，所以不在try块中
        ProcessContext ctx = initd.parse(jp, scheduled);
        // 获取driver
        if (!StringUtils.isEmpty(scheduled.cache())) {
            ctx.setCacheDriver(cacheDriverFactory.getCacheDriver(scheduled.cache()));
        }
        // TODO 将 处理数据 转变为
        try {
            logger.info("ctx_id: {} , start to retrive data from: {} ", ctx.getCtxId(), jp.getSignature()
                    .toLongString());
            // TODO 将driver等的流程放入这里
            Object ret = initd.start(ctx);
            logger.info("data got successfully ctx_id: " + ctx.getCtxId() + ", data: " + ret);
            return ret;
        } catch (SchedAopException exp) {
            // be careful, this kind of exception may caused by your incorrect code
            logger.error("ctx_id: {} ,revive error occors in cache aop , caused by :", ctx.getCtxId(), exp);
            if (ctx.getBatchSize() > 0) {
                return initd.startPlainWithBatch(ctx);
            }
            // return the original call
            return jp.proceed(jp.getArgs());
        } catch (RuntimeException rtExp) {
            // swallow the runtime exception
            logger.error("ctx_id: {} , revive runtime exception occurs in cache aop , caused by :", ctx.getCtxId(),
                    rtExp);
            // be careful about this kind of exception, the cache server may just crash
            if (ctx.getBatchSize() > 0) {
                return initd.startPlainWithBatch(ctx);
            }
            return jp.proceed(jp.getArgs());
        }
    }
}
