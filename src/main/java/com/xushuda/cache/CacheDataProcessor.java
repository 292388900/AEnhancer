package com.xushuda.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xushuda.cache.driver.CacheDriver;
import com.xushuda.cache.driver.CacheDriverFactory;
import com.xushuda.cache.exception.UnexpectedStateException;
import com.xushuda.cache.model.Aggregation;
import com.xushuda.cache.model.AnnotationInfo;
import com.xushuda.cache.model.SignatureInfo;

/**
 * 与cacheDrive的接口交互，处理Cache
 * 
 * @author xushuda
 *
 */
@Service
public class CacheDataProcessor {

    @Autowired
    private CacheDriverFactory fac; // TODO 这里本来是自动加载的，如果要增加可配置性，就改为配置application context

    private static final Logger logger = LoggerFactory.getLogger(CacheDataProcessor.class);

    /**
     * 非聚合类的处理方式。
     * 
     * @param annotation
     * @param jp
     * @return
     * @throws Throwable
     */
    public Object processNormal(AnnotationInfo annotation, ProceedingJoinPoint jp) throws Throwable {
        Object[] args = jp.getArgs().clone(); // a new copy
        CacheDriver driver = fac.getCacheDriver(annotation.getDriverClass());
        Object data = driver.retrieve(driver.id(args));
        if (null == data) {
            logger.info("data doesn't exists in cache, start to call the target process with args {}", args);
            data = jp.proceed(args);
            if (null != data) {
                driver.set(driver.id(args), data, annotation.getExpiration());
                logger.info("get data: {},and saved to cache", data);
            } else {
                logger.warn("the data got from target procedure is still null");
            }
        }
        return data;
    }

    /**
     * 聚合类的处理
     * 
     * @param signature
     * @param annotation
     * @param jp
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("rawtypes")
    public Object processAggregated(SignatureInfo signature, AnnotationInfo annotation, ProceedingJoinPoint jp)
            throws Throwable {
        Object[] args = jp.getArgs().clone(); // a new copy !!important!!
        if (args[signature.getPosition()] == null) {
            throw new NullPointerException("the argument of at position: " + signature.getPosition()
                    + "(Collection or Map) should not be null ");
        }
        Aggregation orignalAggregatedParam =
                new Aggregation(signature.getAggParamType(), args[signature.getPosition()]);
        Aggregation unCachedParam = new Aggregation(signature.getAggParamType());
        Aggregation result = new Aggregation(signature.getRetType());
        Aggregation unCachedResult = new Aggregation(signature.getRetType());
        CacheDriver driver = fac.getCacheDriver(annotation.getDriverClass());

        // 原始参数中的聚合类参数如果为空，那么直接返回空的结果集。
        if (orignalAggregatedParam.isEmpty()) {
            return result.toInstance();
        }
        // 根据param中获取key，并且从cacheDriver中取得数据
        Object key = null;
        List<String> keys = new ArrayList<String>();

        for (Object obj : orignalAggregatedParam) {
            if (null == obj) {
                logger.error("the object in parameter is null, which will be skipped");
                continue;
            }
            key = annotation.extParam(obj);
            args[signature.getPosition()] = key;
            keys.add(driver.id(args));
        }

        // 获取缓存中的数据
        List<Object> cachedResult = driver.getAll(keys.toArray(new String[0]));
        // 接口会按照入参的顺序返回结果，对于未命中的参数会返回null.所以结果集合大小必须和keys一样
        assertSize(cachedResult, keys);
        // 遍历对比key和cache中的结果
        Iterator pIter = orignalAggregatedParam.iterator();
        Iterator rIter = cachedResult.iterator();
        while (pIter.hasNext()) {
            Object data = rIter.next();
            Object param = pIter.next();
            if (data == null) {
                unCachedParam.add(param);
            } else {
                result.add(data);
            }
        }
        // 从接口query新的数据，并加入result集合
        if (!unCachedParam.isEmpty()) {

            // the argument
            for (Aggregation splited : unCachedParam.split(annotation.getBatchSize())) {
                args[signature.getPosition()] = splited.toInstance();
                // get the data from target process
                logger.info("unCached keys exist, call the target process to get data, args are : {}", args);
                Object rawResult = jp.proceed(args);
                if (null != rawResult) {
                    logger.info("data is get from target process : {}", rawResult);
                    unCachedResult.addAll(rawResult);
                } else {
                    logger.warn("the data is not avaliable from the procedure");
                }
            }
            // 集合大小应该一样
            assertSize(unCachedParam, unCachedResult);
            if (!unCachedResult.isEmpty()) {

                // 生成批量缓存的Map
                Map<String, Serializable> datas = new HashMap<String, Serializable>();
                for (Object resultElement : unCachedResult) {
                    if (null == resultElement) {
                        logger.error("the element got from procedure contains nill, which won't be saved to cache");
                        continue;
                    }
                    args[signature.getPosition()] = annotation.extResult(resultElement);
                    datas.put(driver.id(args), (Serializable) resultElement);
                }

                // 缓存这部分数据
                logger.info("unCached data (order is disrupted) will be saved (expiration: {}) to cahce : {}",
                        annotation.getExpiration(), datas);
                driver.setAll(datas, annotation.getExpiration());
                // 加入result的集合
                result.add(unCachedResult);
            }
        }

        // get the target object
        return result.toInstance();
    }

    /**
     * 只有batch的处理，可能是缓存组件失效
     * 
     * @param signature
     * @param annotation
     * @param jp
     * @return
     * @throws Throwable
     */
    public Object processAggregatedWithoutCache(SignatureInfo signature, AnnotationInfo annotation,
            ProceedingJoinPoint jp) throws Throwable {
        Object[] args = jp.getArgs().clone(); // a new copy !!important!!
        if (args[signature.getPosition()] == null) {
            throw new NullPointerException("the argument of at position: " + signature.getPosition()
                    + "(Collection or Map) should not be null ");
        }
        Aggregation orignalAggregatedParam =
                new Aggregation(signature.getAggParamType(), args[signature.getPosition()]);
        Aggregation result = new Aggregation(signature.getRetType());
        // 直接分批调用
        if (!orignalAggregatedParam.isEmpty()) {
            // the argument
            for (Aggregation splited : orignalAggregatedParam.split(annotation.getBatchSize())) {
                args[signature.getPosition()] = splited.toInstance();
                // get the data from target process
                logger.info("unCached keys exist, call the target process to get data, args are : {}", args);
                Object rawResult = jp.proceed(args);
                if (null != rawResult) {
                    logger.info("data is get from target process : {}", rawResult);
                    result.addAll(rawResult);
                } else {
                    logger.warn("the data is not avaliable from the procedure");
                }
            }
        }
        return result.toInstance();
    }

    /**
     * the size of return value must match the size of key
     * 
     * @param ret
     * @param key
     * @throws UnexpectedStateException
     */
    private void assertSize(List<Object> ret, List<String> key) throws UnexpectedStateException {
        if (ret == null || key == null || ret.size() != key.size()) {
            throw new UnexpectedStateException("error return size " + (null == ret ? 0 : ret.size())
                    + " not eauals to key size " + (null == key ? 0 : key.size()));
        }
    }

    /**
     * same as AssertSize(List ,List)
     * 
     * @param ret
     * @param key
     * @throws UnexpectedStateException
     */
    private void assertSize(Aggregation ret, Aggregation key) throws UnexpectedStateException {
        if (ret == null || key == null || ret.size() != key.size()) {
            throw new UnexpectedStateException("error return size " + (null == ret ? 0 : ret.size())
                    + " not eauals to key size " + (null == key ? 0 : key.size()));
        }
    }
}
