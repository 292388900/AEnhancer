package com.baidu.ascheduler.exec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;

/**
 * TODO 提供BlockingQeue的等待超时控制，提供隔离控制
 * 
 * @author xushuda
 *
 */
public class ProcessExecutor {
    private final static Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);
    private ExecutorService execService;
    private static volatile ProcessExecutor instance;

    public static ProcessExecutor getInstance() {
        if (null == instance) {
            synchronized (ProcessExecutor.class) {
                if (null == instance) {
                    instance = new ProcessExecutor();
                }
            }
        }
        return instance;
    }

    private ProcessExecutor() {
        int corePoolSize = 5;
        int maximumPoolSize = 10;
        int keepAliveTime = 5000;
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
        execService =
                new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        // execService = Executors.newScheduledThreadPool(corePoolSize, threadFactory);
        // execService = Executors.newFixedThreadPool(3,threadFactory);
        logger.info("executor is inited");

    }

    /**
     * 提交要执行的任务
     * 
     * @param processor
     * @param ctx
     * @param param
     * @return
     */
    public Future<Object> submitProcess(final DecoratableProcessor processor, final ProcessContext ctx,
            final Object param) {
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
        return execService.submit(callable);
    }

    public List<Future<Object>> submitProcess(final DecoratableProcessor processor, final ProcessContext ctx,
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
        return execService.invokeAll(tasks, timeout, TimeUnit.MILLISECONDS);
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
    public List<Future<Object>> submitProcess(final DecoratableProcessor processor, final ProcessContext ctx,
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
        return execService.invokeAll(tasks);
    }

    // ????
    public List<Future<Object>> submitProcess(final DecoratableProcessor processor, final ProcessContext ctx,
            final List<Object> params, final CountDownLatch cdl) throws InterruptedException {
        // List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        List<Future<Object>> ret = new ArrayList<Future<Object>>();
        for (final Object param : params) {
            // 需要执行的方法
            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        Object ret = processor.doo(ctx, param);
                        cdl.countDown();
                        return ret;
                    } catch (Exception e) {
                        logger.error("error in executor, just throw to upper class");
                        throw e;
                    } catch (Throwable e) {
                        logger.error("error in executor {} , throw ExcutionException", e);
                        throw new ExecutionException(e);
                    }
                }
            };
            ret.add(execService.submit(callable));
        }
        return ret;
    }
}
