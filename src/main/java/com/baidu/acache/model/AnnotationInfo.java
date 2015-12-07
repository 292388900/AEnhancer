package com.baidu.acache.model;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.baidu.acache.exception.IllegalParamException;

/**
 * annotation info
 * 
 * @author xushuda
 *
 */
public class AnnotationInfo {

    private Expression extractFromParam;
    private Expression extractFromResult;
    private int[] ignList;
    private int batchSize;
    private int expiration;
    private String driverBeanName;
    private String nameSpace;

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

    public AnnotationInfo(String etParam, String etResult, int batchSize, String driverBeanName, int expiration,
            int[] ignList, String nameSpace) {
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
        this.nameSpace = nameSpace;
    }

    /**
     * 是否使用聚合方式缓存，
     * 
     * @return true，使用aggr方式缓存，即对对象和
     * @throws IllegalParamException
     */
    public boolean aggrInvok() {
        return null != extractFromResult || null != extractFromParam;
    }

    public String getNameSpace() {
        return nameSpace;
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
