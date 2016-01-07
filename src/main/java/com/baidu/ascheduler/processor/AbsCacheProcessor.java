package com.baidu.ascheduler.processor;

import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ascheduler.exception.UnexpectedStateException;
import com.baidu.ascheduler.model.Aggregation;
import com.baidu.ascheduler.model.ProcessContext;

public abstract class AbsCacheProcessor implements DecoratableProcessor {
    private static final String FIXED = "FIXED";

    private static final String KEY_SEPERATOR = ",";
    
    private Logger logger = LoggerFactory.getLogger(AbsCacheProcessor.class);

    /**
     * 获取参数对应的key
     * 
     * @param args
     * @param ctx
     * @return
     */
    protected String getKey(Object[] args, ProcessContext ctx) {
        if (args != null) {
            // get the key's prefix
            StringBuilder key = new StringBuilder();
            // 标记ignore列表中的位置
            BitSet ign = new BitSet(args.length);
            for (int i : ctx.getIgnoreList()) {
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
                logger.info("in name space '{}' ,key for org data {} is: '{}'", ctx.getNameSpace(), args, ret);
                return ret;
            }
            // possible, as all the argument is in ignore list
            logger.debug("the arguments is not null but the key is null key's name space: '{}'", ctx.getNameSpace());
        }
        logger.info("in name space: '{}' ,key for no argument(or all arg in ignore list) function is: '{}'",
                ctx.getNameSpace(), FIXED);
        return FIXED;
    }
    
    /**
     * same as AssertSize(List ,List)
     * 
     * @param ret
     * @param key
     * @throws UnexpectedStateException
     */
    protected void assertSize(Aggregation ret, Aggregation key) throws UnexpectedStateException {
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
    protected void assertSize(List<Object> ret, List<String> key) throws UnexpectedStateException {
        if (ret == null || key == null || ret.size() != key.size()) {
            throw new UnexpectedStateException("error return size " + (null == ret ? 0 : ret.size())
                    + " not eauals to key size " + (null == key ? 0 : key.size()));
        }
    }
}
