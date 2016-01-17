package com.baidu.aenhancer.entry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.baidu.aenhancer.core.context.AopContext;
import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.impl.InceptProcessor;
import com.baidu.aenhancer.exception.EnhancerCheckedException;

/**
 * 定义修饰的切点（point cut）和它的连接点（join point）处理
 * 
 * @author xushuda
 *
 */
@Aspect
public class EnhancerAspect implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EnhancerAspect.class);

    private ApplicationContext applicationContext;

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
    public Object around(ProceedingJoinPoint jp, Enhancer scheduled) throws Throwable {
        // 预处理
        ProcessContext ctx = null;
        // 真正的数据处理阶段
        try {
            // 这个起始Processor其实不引用别的Processor，仅仅调用了上下文中的hook
            Processor initd = new InceptProcessor(null);
            ctx = new AopContext(scheduled, jp, applicationContext);
            logger.info("ctx_id: {} start : \"{}\"", ctx.getCtxId(), jp.getSignature().toLongString());
            Object ret = initd.doo(ctx, ctx.getArgs());
            logger.info("ctx_id: {} finished, ret: \"{}\"", ctx.getCtxId(), ret);
            return ret;
        } catch (EnhancerCheckedException e) {
            logger.error(" ctxId: {} ,method: \"{}\", fatal error occors in processing , caused by: ",
                    ctx != null ? ctx.getCtxId() : 0, jp.getSignature().toLongString(), e);
            return jp.proceed(jp.getArgs());
        } catch (RuntimeException rtExp) {
            // swallow the runtime exception
            logger.error("ctx_id: {} ,  exception occurs in cache aop , caused by: ", ctx != null ? ctx.getCtxId() : 0,
                    rtExp);
            throw rtExp;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
