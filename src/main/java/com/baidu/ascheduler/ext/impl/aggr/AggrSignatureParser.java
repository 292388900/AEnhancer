package com.baidu.ascheduler.ext.impl.aggr;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.baidu.ascheduler.exception.IllegalParamException;

public class AggrSignatureParser {
    
    /**
     * 解析 函数签名
     * 
     * @param jp
     * @return
     * @throws IllegalParamException ,如果不是Aggr类的调用
     */
    public static AggrSignatureInfo parseSignature(ProceedingJoinPoint jp) throws IllegalParamException {
        Class<?> retType = ((MethodSignature) jp.getSignature()).getReturnType();
        Class<?>[] paramTypes = ((MethodSignature) jp.getSignature()).getParameterTypes();
        Class<?> aggParamType = null;
        int aggregation = 0;
        int position = 0; // the position of the aggregation

        if (null != paramTypes) {
            for (Class<?> paramType : paramTypes) {
                if (Aggregation.isAggregationType(paramType)) {
                    // fail fast 不符合期望
                    if (++aggregation > 1) {
                        // should not reach here
                        // 这个异常不应该在发行版本中出现，一定是annotation的参数不正确（与signature不符合）
                        throw new IllegalParamException("at most one Aggregation is allowed in param");
                    }
                    aggParamType = paramType;
                }
                // aggregation = 0 表示还没有聚合类，position递增，
                // aggregation = 1 表示有聚合类 position指向当前聚合类在参数中的位置
                position += 1 - aggregation;
            }
        }
        return new AggrSignatureInfo(retType, aggParamType, position, jp.getSignature());
    }
}
