package com.baidu.aenhancer.ext.impl.aggr;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.CacheProxy;
import com.baidu.aenhancer.exception.CodingError;
import com.baidu.aenhancer.exception.IllegalParamException;
import com.baidu.aenhancer.exception.UnexpectedStateException;

/**
 * 主要处理对集合方法的缓存
 * 
 * @author xushuda
 *
 */
@Component("aggrCache")
@Scope("prototype")
public class AggrCacher implements CacheProxy {

    private static final String FIXED = "FIXED";

    private static final String KEY_SEPERATOR = ",";

    private final static Logger logger = LoggerFactory.getLogger(AggrCacher.class);

    private AggrSignatureInfo signature;

    private ProcessContext ctx;

    // annotation
    private Aggr aggr;

    // 编译后的spEL
    private Expression extractFromParam;
    private Expression extractFromResult;

    private boolean allCached = false;

    // result
    private Aggregation aggrResult;

    // params indicate the mapping result is not in cache,sub collection from args[signature.getPosition()]
    private Aggregation uncachedAggrParam;

    private Object[] args;

    private CacheDriver driver;

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
        signature = AggrSignatureParser.parseSignature(jp);
        try {
            aggr =
                    (jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
                            ((MethodSignature) jp.getSignature()).getParameterTypes())).getAnnotation(Aggr.class);
        } catch (Exception e) {
            throw new CodingError("error get signature, cause :", e);
        }
        if (null == aggr) {
            throw new CodingError("AggrCacher but ,no @Aggr exists");
        }
        SpelExpressionParser parser = new SpelExpressionParser();
        String etParam = aggr.param();
        String etResult = aggr.result();
        if (!StringUtils.isEmpty(etParam)) {
            extractFromParam = parser.parseExpression(etParam);
        }
        if (!StringUtils.isEmpty(etResult)) {
            extractFromResult = parser.parseExpression(etResult);
        }
        driver = context.getBean(aggr.cache(), CacheDriver.class);
        // TODO 验证有效性
    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
        this.ctx = ctx;
    }

    @Override
    public Object retrieveFromCache(Object[] args) {
        this.args = args;
        Aggregation aggrParam = new Aggregation(signature.getAggParamType(), args[signature.getPosition()]);
        aggrResult = new Aggregation(signature.getRetType());
        // 只用复制一次
        Object[] newParam = new Object[args.length];
        System.arraycopy(args, 0, newParam, 0, args.length);
        // 获取key组成的list
        List<String> keys = new ArrayList<String>();
        for (Object obj : aggrParam) {
            if (null == obj) {
                logger.error("ctx_id: {} the object in parameter is null, which will be skipped", ctx.getCtxId());
                continue;
            }
            // 替换相应位置上的对象
            newParam[signature.getPosition()] = obj;
            logger.info("params {} will be used to generate key", newParam);
            keys.add(getKey(newParam));
        }

        // 获取缓存中的数据
        List<Object> cachedResult = driver.getAll(keys, getNameSpace());
        // 遍历对比key和cache中的结果
        Iterator<?> pIter = aggrParam.iterator();
        Iterator<?> rIter = cachedResult.iterator();
        // 未缓存的参数
        uncachedAggrParam = new Aggregation(signature.getAggParamType());
        while (pIter.hasNext()) {
            // 同步遍历，同步跳过
            Object resultElement = rIter.next();
            Object paramElement = pIter.next();
            // driver返回的null值被认为是没有缓存的
            if (resultElement == null) {
                uncachedAggrParam.add(paramElement);
            } else {
                aggrResult.add(resultElement);
            }
        }
        // 全部命中缓存
        if (uncachedAggrParam.isEmpty()) {
            allCached = true;
            return aggrResult.toInstance();
        } else {
            allCached = false;
            newParam[signature.getPosition()] = uncachedAggrParam.toInstance();
            return newParam;
        }
    }

    @Override
    public boolean allCached() {
        return allCached;
    }

    @Override
    public Object cacheNcollapse(Object resultFromNxtProc) {
        if (null != resultFromNxtProc) {
            Aggregation unCachedResult = new Aggregation(signature.getRetType(), resultFromNxtProc);
            // 缓存这部分数据
            cacheUnCached(unCachedResult, uncachedAggrParam);
            aggrResult.add(unCachedResult);
        }
        return aggrResult.toInstance();

    }

    /**
     * 获取参数对应的key
     * 
     * @param args
     * @param ctx
     * @return
     */
    protected String getKey(Object[] args) {
        if (args != null) {
            // get the key's prefix
            StringBuilder key = new StringBuilder();
            // 标记ignore列表中的位置
            BitSet ign = new BitSet(args.length);
            for (int i : aggr.ignList()) {
                ign.set(i);
            }
            int pos = 0;
            // 将所有非空，并且不在ignore列表中的参数计入key
            for (Object obj : args) {
                // 使用参数对象的HashCode作为key
                if (obj != null && !ign.get(pos++)) {
                    key.append(KEY_SEPERATOR).append(obj.hashCode());
                }
            }
            // 返回string的key
            String ret = key.toString();
            // empty string will return FIXED
            if (!StringUtils.isEmpty(ret)) {
                logger.info("ctx_id: {} in name space '{}' ,key for org data {} is: '{}'", ctx.getCtxId(),
                        getNameSpace(), args, ret);
                return ret;
            }
            // possible, as all the argument is in ignore list
            logger.debug("ctx_id: {} the arguments is not null but the key is null key's name space: '{}'",
                    ctx.getCtxId(), getNameSpace());
        }
        logger.info("ctx_id: {} in name space: '{}' ,key for no argument(or all arg in ignore list) function is: '{}'",
                ctx.getCtxId(), getNameSpace(), FIXED);
        return FIXED;
    }

    /**
     * 
     * @param unCachedResult
     * @param unCachedParam
     * @param ctx
     * @param driver
     * @throws UnexpectedStateException
     * @throws IllegalParamException
     */
    private void cacheUnCached(Aggregation unCachedResult, Aggregation unCachedParam) {
        assert !unCachedResult.isEmpty();
        // 生成批量缓存的kv
        List<String> unCachedKeys = new LinkedList<String>();
        List<Object> unCachedDatas = new LinkedList<Object>();
        // 只用复制一次
        Object[] newParam = new Object[args.length];
        System.arraycopy(args, 0, newParam, 0, args.length);
        // 根据result获取key
        if (!aggr.sequential()) {
            // 只遍历结果集
            for (Object resultElement : unCachedResult) {
                // 注意：这里跳过了null的结果，如果原来接口返回null，则不将它缓存下来
                if (null == resultElement) {
                    logger.error(
                            "ctx_id: {} the element got from procedure contains nill, which won't be saved to cache",
                            ctx.getCtxId());
                    continue;
                }
                // 替换aggr位置上的参数
                newParam[signature.getPosition()] = extResult(resultElement);
                unCachedKeys.add(getKey(newParam));
                unCachedDatas.add(resultElement);
            }
            assertSize(unCachedDatas, unCachedKeys);
        } else { // rely on result is sequential、
            // 顺序的话，依赖参数与结果集的顺序。所以，大小必须一样
            assertSize(unCachedResult, unCachedParam);
            Iterator<?> urIter = unCachedResult.iterator(); // uncached result iterator
            Iterator<?> upIter = unCachedParam.iterator(); // uncached param iterator
            while (upIter.hasNext()) {
                // 同步遍历结果集和参数集
                Object uData = urIter.next();
                Object uParam = upIter.next();
                if (null != uData) { // 这里与非顺序的是一样的，如果null就不缓存
                    // 替换aggr位置上的参数
                    newParam[signature.getPosition()] = extParam(uParam);
                    unCachedKeys.add(getKey(newParam));
                    unCachedDatas.add(uData);
                }
            }
        }
        // 缓存这部分数据
        logger.info("ctx_id: {} unCached data (order is disrupted size {}) will be saved (expiration: {}) ",
                ctx.getCtxId(), unCachedKeys.size(), aggr.expiration());
        driver.setAll(unCachedKeys, unCachedDatas, aggr.expiration(), getNameSpace());
        // 加入result的集合
        // result.add(unCachedResult)
    }

    /**
     * same as AssertSize(List ,List)
     * 
     * @param ret
     * @param key
     * @throws UnexpectedStateException
     */
    protected void assertSize(Aggregation ret, Aggregation key) {
        if (ret == null || key == null || ret.size() != key.size()) {
            throw new UnexpectedStateException("error return size " + (null == ret ? 0 : ret.size())
                    + " not eauals to key size " + (null == key ? 0 : key.size()));
        }
    }

    /**
     * the size of return value must match the size of key
     * 
     * @param ret
     * @param key
     * @throws UnexpectedStateException
     */
    protected void assertSize(List<Object> ret, List<String> key) {
        if (ret == null || key == null || ret.size() != key.size()) {
            throw new UnexpectedStateException("error return size " + (null == ret ? 0 : ret.size())
                    + " not eauals to key size " + (null == key ? 0 : key.size()));
        }
    }

    private String getNameSpace() {
        return StringUtils.isEmpty(aggr.nameSpace()) ? signature.getSignature() : aggr.nameSpace();
    }

    public Object extParam(Object paramElement) {
        // assertBatch();
        if (null != extractFromParam) {
            return extractFromParam.getValue(new StandardEvaluationContext(paramElement));
        }
        return paramElement;
    }

    /**
     * 从result中获取key
     * 
     * @param resultElement
     * @return key
     * @throws IllegalParamException
     */
    public Object extResult(Object resultElement) {
        if (null == extractFromResult) {
            throw new IllegalParamException("error ext from result, spEl is null");
        }
        return extractFromResult.getValue(resultElement); // TODO context??
    }
}
