package com.baidu.ascheduler.processor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.exec.DecoratableProcessor;
import com.baidu.ascheduler.ext.Splitable;

/**
 * 单线程循环调用split
 * 
 * @author xushuda
 *
 */
public class SyncSplitProcessor extends DecoratableProcessor {

    private Logger logger = LoggerFactory.getLogger(SyncSplitProcessor.class);

    /**
     * 分多次调用下一个processor
     * 
     * @param Object p
     * @throws Throwable
     */
    @Override
    public Object process(ProcessContext ctx, Object p) throws Throwable {
        Splitable spliter = ctx.getSpliter();
        // before
        spliter.beforeProcess(ctx, this);
        // split 参数
        List<Object[]> params = spliter.split((Object[]) p);
        // 结果集合
        List<Object> results = new ArrayList<Object>();
        for (Object[] param : params) {
            logger.info("ctx_id: {} get splited param as : {}", ctx.getCtxId(), param);
            // 从下一个processor获取结果，如果对于Async的方式，可能是返回一个Future对象
            Object rawResult = decoratee.doo(ctx, param);
            // 可能为null
            logger.info("ctx_id: {} the data got from sub processor: {}", ctx.getCtxId(), rawResult);
            results.add(rawResult);
        }
        // 这里调用collapse方法，得到真正的返回结果
        return spliter.collapse(results);
    }

    @Override
    public void preCheck(ProcessContext ctx, Object param) throws SchedAopException {
        if (decoratee == null) {
            throw new UnexpectedStateException("decoratee can't be null for Batch Processor");
        }

    }

    @Override
    protected void postCheck(ProcessContext ctx, Object ret) throws SchedAopException {
        // TODO Auto-generated method stub
        
    }

}