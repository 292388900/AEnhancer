package com.baidu.ascheduler.ext.impl.aggr;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.baidu.ascheduler.context.ProcessContext;
import com.baidu.ascheduler.exception.IllegalParamException;
import com.baidu.ascheduler.exception.SchedAopException;
import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.exec.DecoratableProcessor;
import com.baidu.ascheduler.ext.Splitable;

public final class AggrSpliter implements Splitable {
    private static final Logger logger = LoggerFactory.getLogger(AggrSpliter.class);
    private AggrSignatureInfo signature;
    private int batchSize;

    @Override
    public Object collapse(List<Object> result) throws IllegalParamException, UnexpectedStateException {
        // 将多次调用的值都放入unCachedResult中
        Aggregation resultAggr = new Aggregation(signature.getRetType());
        for (Object deltaResult : result) {
            Aggregation deltaResultAggr = new Aggregation(signature.getRetType(), deltaResult);
            resultAggr.add(deltaResultAggr);
        }
        return resultAggr.toInstance();
    }

    @Override
    public List<Object[]> split(Object[] args) throws SchedAopException {
        List<Object[]> result = new ArrayList<Object[]>();
        // 将数据放入Aggrgation中，再调用Aggregation来split
        Aggregation param = new Aggregation(signature.getAggParamType(), (args)[signature.getPosition()]);
        if (param.isEmpty()) {
            // TODO this will still do?
        }
        // the argument，如果必要（batch）会分为多批
        for (Aggregation splited : param.split(batchSize)) {
            // 替换后的参数 设置为结果，作为下一个processor的参数
            Object[] newParam = new Object[args.length];
            System.arraycopy(args, 0, newParam, 0, args.length);
            newParam[signature.getPosition()] = splited.toInstance();
            logger.info("data is splited by {}, splited param: {}", batchSize, newParam);
            result.add(newParam);
        }
        return result;

    }

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws IllegalParamException {
        Aggr annotation = ((MethodSignature) jp.getSignature()).getMethod().getAnnotation(Aggr.class);
        if (null != annotation && annotation.batchSize() > 0) {
            this.batchSize = annotation.batchSize();
        }
        // TODO else skip
        signature = AggrSignatureParser.parseSignature(jp);
    }

    @Override
    public void beforeProcess(ProcessContext ctx, DecoratableProcessor currentProcess) {
        // TODO Auto-generated method stub

    }
}
