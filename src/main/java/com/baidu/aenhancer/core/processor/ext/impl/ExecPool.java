package com.baidu.aenhancer.core.processor.ext.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.conf.Configurable;
import com.baidu.aenhancer.core.conf.ExecutorPoolConfig;
import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;

public class ExecPool implements Configurable<ExecutorPoolConfig> {
    public static final String SHARED = "SHARED_POOL";

    private final static Logger logger = LoggerFactory.getLogger(ExecPool.class);

    private volatile ExecutorPoolConfig poolConfig;
    private volatile ExecutorService execPool;
    private String group;

    public ExecPool(String group) {
        this.group = group;
        this.poolConfig = new ExecutorPoolConfig();
    }

    @Override
    public ExecutorPoolConfig getConfig() {
        return poolConfig;
    }

    @Override
    public void config(ExecutorPoolConfig config) {
        this.poolConfig = config;
    }

    @Override
    public String namespace() {
        return StringUtils.isEmpty(group) ? SHARED : group + ".thread.pool";
    }

    /**
     * lazy init ExecPool
     * 
     * @return
     */
    private ExecutorService getExecPool() {
        if (null == execPool) {
            synchronized (this) {
                if (null == execPool) {

                    TimeUnit unit = TimeUnit.MILLISECONDS;
                    BlockingQueue<Runnable> workQueue =
                            poolConfig.getQueueSize() > 0 ? new ArrayBlockingQueue<Runnable>(poolConfig.getQueueSize())
                                    : new LinkedBlockingQueue<Runnable>();
                    // 设置为DaemonThread
                    ThreadFactory threadFactory = new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            logger.info("a new thread is created by ExecPool");
                            Thread td = new Thread(r);
                            td.setDaemon(true);
                            return td;
                        }
                    };
                    execPool =
                            new ThreadPoolExecutor(poolConfig.getCorePoolSize(), poolConfig.getMaximumPoolSize(),
                                    poolConfig.getKeepAliveTime(), unit, workQueue, threadFactory);
                }
            }
        }
        return execPool;
    }

    /**
     * 提交要执行的任务
     * 
     * @param processor
     * @param ctx
     * @param param
     * @return
     */
    public Future<Object> submitProcess(final Processor processor, final ProcessContext ctx, final Object param) {
        // 需要执行的方法
        Callable<Object> callable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    return processor.doo(ctx, param);
                } catch (Exception e) {
                    logger.error("error in executor, just throw to upper class");
                    throw e;
                } catch (Throwable e) {
                    logger.error("error in executor {} , throw ExcutionException", e);
                    throw new ExecutionException(e);
                }
            }
        };
        return getExecPool().submit(callable);
    }

    /**
     * 提交带有超时的任务
     * 
     * @param group
     * @param processor
     * @param ctx
     * @param params
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public List<Future<Object>> submitProcess(final Processor processor, final ProcessContext ctx,
            final List<Object> params, long timeout) throws InterruptedException {
        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        for (final Object param : params) {
            // 需要执行的方法
            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        return processor.doo(ctx, param);
                    } catch (Exception e) {
                        logger.error("error in executor, just throw to upper class");
                        throw e;
                    } catch (Throwable e) {
                        logger.error("error in executor {} , throw ExcutionException", e);
                        throw new ExecutionException(e);
                    }
                }
            };
            tasks.add(callable);
        }
        return getExecPool().invokeAll(tasks, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 没有timeout
     * 
     * @param processor
     * @param ctx
     * @param params
     * @return
     * @throws InterruptedException
     */
    public List<Future<Object>> submitProcess(final Processor processor, final ProcessContext ctx,
            final List<Object> params) throws InterruptedException {
        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        for (final Object param : params) {
            // 需要执行的方法
            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        return processor.doo(ctx, param);
                    } catch (Exception e) {
                        logger.error("error in executor, just throw to upper class");
                        throw e;
                    } catch (Throwable e) {
                        logger.error("error in executor {} , throw ExcutionException", e);
                        throw new ExecutionException(e);
                    }
                }
            };
            tasks.add(callable);
        }
        return getExecPool().invokeAll(tasks);
    }
}
