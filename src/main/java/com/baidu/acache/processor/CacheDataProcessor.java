package com.baidu.acache.processor;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.acache.driver.CacheDriver;
import com.baidu.acache.exception.UnexpectedStateException;
import com.baidu.acache.model.Aggregation;
import com.baidu.acache.model.MethodInfo;

/**
 * 与cacheDrive的接口交互，处理Cache 所有获取为null的元素，视为可能远程的失败，不做存储<br>
 * 实际上，被修饰的函数本身应该（必须）处理错误的信息，仅将需要缓存的数据返回。
 * 
 * @author xushuda
 *
 */
public class CacheDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CacheDataProcessor.class);

    private static final String FIXED = "FIXED";

    private static final String KEY_SEPERATOR = ",";

    /**
     * 非聚合类的处理方式。
     * 
     * @param annotation
     * @param jp
     * @return
     * @throws Throwable
     */
    public Object processNormal(MethodInfo methodInfo, CacheDriver driver) throws Throwable {
        Object[] args = methodInfo.getArgs(); // a new copy
        // CacheDriver driver = fac.getCacheDriver(methodInfo.getDriver());
        Object data = driver.get(getKey(args, methodInfo), methodInfo.getNameSpace());
        logger.info("data retrieved for key {} is {}", args, data);
        if (null == data) {
            logger.info("data doesn't exists in cache, start to call the target process with args {}", args);
            data = methodInfo.proceedWith(args);
            if (null != data) {
                driver.set(getKey(args, methodInfo), data, methodInfo.getExpiration(), methodInfo.getNameSpace());
                logger.info("get data: {}, and saved to cache", data);
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
    @SuppressWarnings({ "rawtypes" })
    public Object processAggregated(MethodInfo methodInfo, CacheDriver driver) throws Throwable {
        if (methodInfo.getOrgAggrParam() == null) {
            throw new NullPointerException("the aggregation param (Collection or Map) should not be null ");
        }
        Aggregation orignalAggregatedParam =
                new Aggregation(methodInfo.getAggParamType(), methodInfo.getOrgAggrParam());
        Aggregation unCachedParam = new Aggregation(methodInfo.getAggParamType());
        Aggregation result = new Aggregation(methodInfo.getRetType());
        // CacheDriver driver = fac.getCacheDriver(methodInfo.getDriver());

        // 原始参数中的聚合类参数如果为空，那么直接返回空的结果集。
        if (orignalAggregatedParam.isEmpty()) {
            return result.toInstance();
        }
        // 根据param中获取key，并且从cacheDriver中取得数据
        List<String> keys = new ArrayList<String>();
        for (Object obj : orignalAggregatedParam) {
            if (null == obj) {
                logger.error("the object in parameter is null, which will be skipped");
                continue;
            }
            keys.add(getKey(methodInfo.replaceArgsWithKeys(methodInfo.getKeyFromParam(obj)), methodInfo));
        }

        // 获取缓存中的数据
        List<Object> cachedResult = driver.getAll(keys, methodInfo.getNameSpace());
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
            // 将多次调用的值都放入unCachedResult中
            Aggregation unCachedResult = new Aggregation(methodInfo.getRetType());
            // the argument，如果必要（batch）会分为多批
            for (Aggregation splited : unCachedParam.split(methodInfo.getBatchSize())) {
                // get the data from target process
                logger.info("unCached keys exist, call the target process to get data, keys args are : {}", splited);
                Object rawResult = methodInfo.proceedWith(methodInfo.replaceArgsWithKeys(splited.toInstance()));
                if (null != rawResult) {
                    logger.info("data is get from target process : {}", rawResult);
                    unCachedResult.addAll(rawResult);
                } else {
                    logger.warn("the data is not avaliable from the procedure");
                }
            }
            // 集合大小不用强制一样，多个value对应一个key则会造成覆盖，但多个key对应一个value其实没有问题
            // assertSize(unCachedParam, unCachedResult);
            if (!unCachedResult.isEmpty()) {

                // 生成批量缓存的kv
                List<String> unCachedKeys = new LinkedList<String>();
                List<Object> unCachedDatas = new LinkedList<Object>();

                // 根据result获取key
                if (!methodInfo.relyOnSeqResult()) {
                    // 只遍历结果集
                    for (Object resultElement : unCachedResult) {
                        if (null == resultElement) {
                            logger.error("the element got from procedure contains nill, which won't be saved to cache");
                            continue;
                        }
                        unCachedKeys
                                .add(getKey(methodInfo.replaceArgsWithKeys(methodInfo.getKeyFromResult(resultElement)),
                                        methodInfo));
                        unCachedDatas.add(resultElement);
                    }
                    assertSize(unCachedDatas, unCachedKeys);
                } else { // rely on result is sequential
                    assertSize(unCachedResult, unCachedParam);
                    Iterator urIter = unCachedResult.iterator(); // uncached result iterator
                    Iterator upIter = unCachedParam.iterator(); // uncached param iterator
                    while (upIter.hasNext()) {
                        // 同步遍历结果集和参数集
                        unCachedKeys.add(getKey(
                                methodInfo.replaceArgsWithKeys(methodInfo.getKeyFromParam(upIter.next())), methodInfo));
                        unCachedDatas.add(urIter.next());
                    }
                }

                // 缓存这部分数据
                logger.info("unCached data (order is disrupted size {}) will be saved (expiration: {}) ",
                        unCachedKeys.size(), methodInfo.getExpiration());
                driver.setAll(unCachedKeys, unCachedDatas, methodInfo.getExpiration(), methodInfo.getNameSpace());
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
    public Object processAggregatedWithoutCache(MethodInfo methodInfo) throws Throwable {
        if (methodInfo.getOrgAggrParam() == null) {
            throw new NullPointerException("the argument for aggrgation as (Collection or Map) should not be null ");
        }
        Aggregation orignalAggregatedParam =
                new Aggregation(methodInfo.getAggParamType(), methodInfo.getOrgAggrParam());
        Aggregation result = new Aggregation(methodInfo.getRetType());
        // 直接分批调用
        if (!orignalAggregatedParam.isEmpty()) {
            // the argument
            for (Aggregation splited : orignalAggregatedParam.split(methodInfo.getBatchSize())) {
                // get the data from target process
                Object rawResult = methodInfo.proceedWith(methodInfo.replaceArgsWithKeys(splited.toInstance()));
                if (null != rawResult) {
                    logger.info("data is got from target process : {}", rawResult);
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

    /**
     * 获取参数对应的key
     * 
     * @param args
     * @param methodInfo
     * @return
     */
    private String getKey(Object[] args, MethodInfo methodInfo) {
        if (args != null) {
            // get the key's prefix
            StringBuilder key = new StringBuilder();
            // 标记ignore列表中的位置
            BitSet ign = new BitSet(args.length);
            for (int i : methodInfo.getIgnoreList()) {
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
            if (!ret.equals("")) {
                logger.info("in name space '{}' ,key for org data {} is: '{}'", methodInfo.getNameSpace(), args, ret);
                return ret;
            }
            // possible, as all the argument is in ignore list
            logger.debug("the arguments is not null but the key is null key's name space: '{}'",
                    methodInfo.getNameSpace());
        }
        logger.info("in name space: '{}' ,key for no argument(or all arg in ignore list) function is: '{}'",
                methodInfo.getNameSpace(), FIXED);
        return FIXED;
    }
}
