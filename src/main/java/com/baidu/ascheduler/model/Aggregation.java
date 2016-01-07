package com.baidu.ascheduler.model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import com.baidu.ascheduler.exception.IllegalParamException;

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

    public static boolean isSequentialType(Class<?> cls) {
        return Object[].class.isAssignableFrom(cls) || List.class.isAssignableFrom(cls)
                || Queue.class.isAssignableFrom(cls); // linked list 
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
     * 实例能赋值给cls（接口或者类）
     * 
     * @param cls
     * @param instance
     */
    private void assertAssignable(Class<?> cls, Object instance) {
        if (!cls.isAssignableFrom(instance.getClass())) {
            throw new IllegalArgumentException("cls: " + cls + " is not supported aggregation");
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

        // target是map的子类
        if (Map.class.isAssignableFrom(target)) {
            if (!target.isInterface() && !Modifier.isAbstract(target.getModifiers())) {
                aggregated = target.newInstance();
                // 如果是有序map
            } else if (target.getClass().isAssignableFrom(SortedMap.class)) {
                aggregated = new TreeMap();
            } else {
                // 默认就是无序map
                aggregated = new HashMap();
            }

            // add data
            for (Object obj : datas) {
                ((Map) aggregated).put(((Cell) obj).getKey(), ((Cell) obj).getValue());
            }

            // target是Collection的子类
        } else if (Collection.class.isAssignableFrom(target)) {
            if (!target.isInterface() && !Modifier.isAbstract(target.getModifiers())) {
                aggregated = target.newInstance();
            } else if (target.getClass().isAssignableFrom(LinkedList.class)) {
                aggregated = new LinkedList();
            } else if (target.getClass().isAssignableFrom(HashSet.class)) {
                aggregated = new HashSet();
            } else {
                aggregated = new ArrayList();
            }

            // add data
            for (Object obj : datas) {
                ((Collection) aggregated).add(obj);
            }
        } else if (Object[].class.isAssignableFrom(target)) {
            Class<?> componentType = target.getComponentType();
            aggregated = Array.newInstance(componentType, datas.size());
            // add data
            int pos = 0;
            for (Object obj : datas) {
                Array.set(aggregated, pos++, componentType.cast(obj));
            }
        } else {
            throw new IllegalParamException("target class can't match any known collection " + target);
        }
        // 最后需要再判断一次
        assertAssignable(target, aggregated);
        return aggregated;
    }

    @Override
    public Iterator iterator() {
        return datas.iterator();
    }

    /**
     * 获取游戏聚合类型的迭代器
     * 
     * @param instance
     * @return
     * @throws IllegalParamException
     */
    public Iterator getSeqIteratorFromInstance(Object instance) throws IllegalParamException {
        if (null == instance) {
            throw new NullPointerException("the instance must not be null");
        }
        if (SortedMap.class.isAssignableFrom(instance.getClass())) {
            return ((SortedMap) instance).entrySet().iterator();
        } else if (SortedSet.class.isAssignableFrom(instance.getClass())) {
            return ((SortedSet) instance).iterator();
        } else if (Queue.class.isAssignableFrom(instance.getClass())) {
            return Arrays.asList(instance).iterator();
        } else {
            throw new IllegalParamException("the instance is not an apporiate class: " + instance.getClass());
        }
    }

    /**
     * 将集合按照batchSize来split成多个
     * 
     * @param batchSize
     * @return List Aggregation列表
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
