package com.baidu.acache.model;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.baidu.acache.exception.IllegalParamException;

/**
 * aggregation 类型, 可以处理的类型有Map的子类，Collection的子类，还有数组类型 <br>
 * ps:
 * 
 * @author xushuda
 *
 */
@SuppressWarnings("rawtypes")
public class Aggregation implements Iterable {

    private List<Object> datas;
    private Class<?> target;

    /**
     * wrap map.entry
     * 
     * @author xushuda
     *
     */
    static class Cell implements Serializable, Map.Entry {

        /**
         * default serialVersionUID
         */
        private static final long serialVersionUID = 1L;
        private Object value;
        private Object key;

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public void setKey(Object key) {
            this.key = key;
        }

        public Object setValue(Object value) {
            this.value = value;
            return value;
        }

        public Cell(Object key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    public static boolean isAggregationType(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)
                || Object[].class.isAssignableFrom(clazz);
    }

    private void assertAggregationType() throws IllegalParamException {
        if (!isAggregationType(target)) {
            throw new IllegalParamException("target class is not an appropriate aggregation type");
        }
    }

    private void assertMap() {
        if (!Map.class.isAssignableFrom(target)) {
            throw new IllegalArgumentException(
                    "target class is not an appropriate aggregation type : is not a sub class of map");
        }
    }

    /**
     * an aggregation who's toInstance method will return the target type
     * 
     * @param target
     * @throws IllegalParamException
     */
    public Aggregation(Class<?> target) throws IllegalParamException {
        this.target = target;
        datas = new LinkedList<Object>();
        assertAggregationType();
    }

    public Aggregation(Class<?> target, Object input) throws IllegalParamException {
        this(target);
        addAll(input);
    }

    /**
     * if the input object is not appropriate, class cast exception will be thrown
     * 
     * @param input
     */
    public void addAll(Object input) {
        if (null == input) {
            throw new NullPointerException("the null object can't be inserted into Aggregation");
        }
        // 根据目标类型来组织数据，entry会被转化为cell
        if (Map.class.isAssignableFrom(target)) {
            for (Object entry : ((Map) input).entrySet()) {
                Cell cell = new Cell(((Map.Entry) entry).getKey(), ((Map.Entry) entry).getValue());
                datas.add(cell);
            }
        } else if (Collection.class.isAssignableFrom(target)) {
            for (Object element : ((Collection) input)) {
                datas.add(element);
            }
        } else {
            for (Object obj : ((Object[]) input)) {
                datas.add(obj);
            }
        }
    }

    /**
     * the entry will be converted to cell, the cell or other object will be inserted directly
     * 
     * @param obj
     */
    public void add(Object obj) {
        if (null == obj) {
            throw new NullPointerException("the null object can't be inserted into Aggregation");
        }
        // 加入entry，将会被转化为cell
        if (obj instanceof Map.Entry) {
            assertMap();
            datas.add(new Cell(((Map.Entry) obj).getKey(), ((Map.Entry) obj).getValue()));
        } else {
            datas.add(obj);
        }
    }

    /**
     * 将另一个aggregation类中的数据加入到当前对象
     * 
     * @param agg
     */
    public void add(Aggregation agg) {
        datas.addAll(agg.datas);
    }

    /**
     * 聚合类是否为空
     * 
     * @return
     */
    public boolean isEmpty() {
        return datas.isEmpty();
    }

    /**
     * 清空
     * 
     */
    public void clear() {
        datas.clear();
    }

    /**
     * 生成对应targetType 的集合（Collection，Map）类, 逻辑是根据构造函数中的class对象构造 <br>
     * 如果class对象是个接口，则取一个实现子类来作为容器 ，如果class是个实体类容器，则调用newInstance生成对象
     * 
     * @return
     * @throws InstantiationException NOTE：如果target是个实体类，但是没有默认构造函数
     * @throws IllegalAccessException
     * @throws IllegalParamException
     */
    @SuppressWarnings({ "unchecked" })
    public Object toInstance() throws InstantiationException, IllegalAccessException, IllegalParamException {
        Object aggregated = null;

        if (Map.class.isAssignableFrom(target)) {
            if (!target.isInterface() && !Modifier.isAbstract(target.getModifiers())) {
                aggregated = target.newInstance();
            } else if (SortedMap.class.isAssignableFrom(target)) {
                aggregated = new TreeMap();
            } else {
                aggregated = new HashMap();
            }

            // add data
            for (Object obj : datas) {
                ((Map) aggregated).put(((Cell) obj).getKey(), ((Cell) obj).getValue());
            }
        } else if (Collection.class.isAssignableFrom(target)) {
            if (!target.isInterface() && !Modifier.isAbstract(target.getModifiers())) {
                aggregated = target.newInstance();
            } else if (List.class.isAssignableFrom(target)) {
                aggregated = new ArrayList();
            } else if (Set.class.isAssignableFrom(target)) {
                if (SortedSet.class.isAssignableFrom(target)) {
                    aggregated = new TreeSet();
                } else {
                    aggregated = new HashSet();
                }

            } else if (Queue.class.isAssignableFrom(target)) {
                aggregated = new LinkedList();
            }

            // add data
            for (Object obj : datas) {
                ((Collection) aggregated).add(obj);
            }
        } else if (Object[].class.isAssignableFrom(target)) {
            aggregated = new Object[datas.size()];
            int pos = 0;

            // add data
            for (Object obj : datas) {
                ((Object[]) aggregated)[pos++] = obj;
            }
        } else {
            throw new IllegalParamException("target class can't match any known collection " + target);
        }
        return aggregated;

    }

    @Override
    public Iterator iterator() {
        return datas.iterator();
    }

    /**
     * 将集合按照batchSize来split成多个
     * 
     * @param batchSize
     * @return
     * @throws IllegalParamException
     */
    public List<Aggregation> split(int batchSize) throws IllegalParamException {
        List<Aggregation> list = new ArrayList<Aggregation>();
        int pos = 0;
        int size = datas.size();
        int tmp;

        if (batchSize > 0) {
            // batch size > 0
            while (pos < size) {
                tmp = pos + batchSize < size ? pos + batchSize : size;
                Aggregation agg = new Aggregation(target);
                agg.datas = datas.subList(pos, tmp);
                list.add(agg);
                pos += batchSize;
            }
        } else {
            // batch size < 0, won't split
            list.add(this);
        }
        return list;
    }

    /**
     * 获取数据的size
     * 
     * @return
     */
    public int size() {
        return datas.size();
    }
}
