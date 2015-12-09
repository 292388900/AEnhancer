package com.baidu.acache.model;

import java.util.Map;

import org.aspectj.lang.Signature;

/**
 * signature info
 * 
 * @author xushuda
 *
 */
public class SignatureInfo {

    public Class<?> getRetType() {
        return retType;
    }

    public Class<?> getAggParamType() {
        return aggParamType;
    }

    public int getPosition() {
        return position;
    }

    public String getSignature() {
        return signature;
    }

    public void setRetType(Class<?> retType) {
        this.retType = retType;
    }

    public void setAggParamType(Class<?> aggParamType) {
        this.aggParamType = aggParamType;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    Class<?> retType;
    Class<?> aggParamType;
    int position;
    String signature;

    /**
     * 
     * @param retType
     * @param aggParamType may be null
     * @param position
     */
    public SignatureInfo(Class<?> retType, Class<?> aggParamType, int position, Signature signature) {
        this.retType = retType;
        this.aggParamType = aggParamType;
        this.position = position;
        this.signature = signature.toLongString();
    }

    public boolean aggrAccessible() {
        return retType != null && aggParamType != null && Aggregation.isAggregationType(retType)
                && Aggregation.isAggregationType(aggParamType);
    }

    /**
     * 返回参数和入参是否都是有序的
     * 
     * @return
     */
    public boolean sequentialAggrAccessible() {
        return retType != null && aggParamType != null && Aggregation.isAggregationType(retType)
                && Aggregation.isSequentialType(aggParamType);
    }

    public boolean retMap() {
        return Map.class.isAssignableFrom(retType);
    }

    /**
     * not used currently
     * 
     * @return
     */
    public boolean kvAggregated() {
        return aggrAccessible() && retMap();
    }

    @Override
    public String toString() {
        return signature;
    }
}
