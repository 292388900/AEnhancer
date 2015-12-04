package com.xushuda.cache.model;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.xushuda.cache.exception.IllegalParamException;

/**
 * annotation info
 * 
 * @author xushuda
 *
 */
public class AnnotationInfo {

    Expression extractFromParam;
    Expression extractFromResult;
    int[] ignList;
    int batchSize;
    int expiration;
    String driverBeanName;

    public boolean validateExt() {
        if (extractFromResult == null && extractFromParam != null) {
            return false;
        }
        return true;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getExpiration() {
        return expiration;
    }

    public String getDriverName() {
        return driverBeanName;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public void setDriverName(String driverBeanName) {
        this.driverBeanName = driverBeanName;
    }

    public AnnotationInfo(String etParam, String etResult, int batchSize, String driverBeanName,
            int expiration, int[] ignList) {
        SpelExpressionParser parser = new SpelExpressionParser();

        // annotation spring expression language
        if (null != etParam && !"".equals(etParam)) {
            extractFromParam = parser.parseExpression(etParam);
        }
        if (null != etResult && !"".equals(etResult)) {
            extractFromResult = parser.parseExpression(etResult);
        }
        this.expiration = expiration;
        this.batchSize = batchSize;
        this.driverBeanName = driverBeanName;
        this.ignList = ignList;
    }

    /**
     * 是否使用batch 批量、拆分式的请求查询方式
     * 
     * @return
     * @throws IllegalParamException
     */
    public boolean aggrInvok() {
        return null != extractFromResult || null != extractFromParam;
    }

    /**
     * 从param中获取key
     * 
     * @param paramElement
     * @return
     * @throws IllegalParamException
     */
    public Object extParam(Object paramElement) throws IllegalParamException {
        assertBatch();
        if (null != extractFromParam) {
            return extractFromParam.getValue(new StandardEvaluationContext(paramElement));
        }
        return paramElement;
    }

    /**
     * 从result中获取key
     * 
     * @param resultElement
     * @return
     * @throws IllegalParamException
     */
    public Object extResult(Object resultElement) throws IllegalParamException {
        assertBatch();
        return extractFromResult.getValue(resultElement);
    }

    /**
     * 
     * @throws IllegalParamException
     */
    public void assertBatch() throws IllegalParamException {
        if (!aggrInvok()) {
            throw new IllegalParamException("only the aggregated param and result can be extracted");
        }
    }

    /**
     * 返回忽略参数列表
     * 
     * @return
     */
    public int[] getIgnList() {
        return ignList;
    }
}
