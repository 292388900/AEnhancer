package com.baidu.aenhancer.core.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.aenhancer.core.context.ProcessContext;

public class ExecutorFactory {
    public static final String SHARED = "SHARED_POOL";
    private final static Logger logger = LoggerFactory.getLogger(ExecutorFactory.class);

    private static volatile ExecutorFactory instance;
    private final ConcurrentHashMap<String, ExecutorService> executorPool;

    public static ExecutorFactory getInstance() {
        if (null == instance) {
            synchronized (ExecutorFactory.class) {
                if (null == instance) {
                    instance = new ExecutorFactory();
                }
            }
        }
        return instance;
    }

    private ExecutorFactory() {
        executorPool = new ConcurrentHashMap<String, ExecutorService>();
    }

    private ExecutorService newExecSrv() {
        int corePoolSize = 5;
        int maximumPoolSize = 10;
        int keepAliveTime = 30;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(5);
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
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * 
     * @param group
     * @return
     */
    private ExecutorService getByGroup(String group) {
        ExecutorService execSrv = executorPool.get(group);

        // 没有就新建
        if (null == execSrv) {
            synchronized (this) {
                if (null == execSrv) {
                    execSrv = newExecSrv();
                    executorPool.put(group, execSrv);
                    logger.info("executor is inited for group: {}", group);
                }
            }
        }
        return execSrv;
    }

    /**
     * 提交要执行的任务
     * 
     * @param processor
     * @param ctx
     * @param param
     * @return
     */
    public Future<Object> submitProcess(String group, final DecoratableProcessor processor, final ProcessContext ctx,
            final Object param) {
        ExecutorService execSrv = getByGroup(group);
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
        return execSrv.submit(callable);
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
    public List<Future<Object>> submitProcess(String group, final DecoratableProcessor processor,
            final ProcessContext ctx, final List<Object> params, long timeout) throws InterruptedException {
        ExecutorService execSrv = getByGroup(group);

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
        return execSrv.invokeAll(tasks, timeout, TimeUnit.MILLISECONDS);
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
    public List<Future<Object>> submitProcess(String group, final DecoratableProcessor processor,
            final ProcessContext ctx, final List<Object> params) throws InterruptedException {
        ExecutorService execSrv = getByGroup(group);

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
        return execSrv.invokeAll(tasks);
    }
}
