package com.baidu.aenhancer.conf.loadtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class LoadtimeConfManager implements ApplicationListener<ContextRefreshedEvent>,
        BeanDefinitionRegistryPostProcessor {
    private boolean isFirstInit = true;
    private final static Logger logger = LoggerFactory.getLogger(LoadtimeConfManager.class);
    private BeanDefinitionRegistry registry;
    private DefaultListableBeanFactory beanFactory;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!isFirstInit) {
            logger.info("application ctx is refreshed again, skip");
            return;
        }
        logger.info("start to load bean dynamically");
        long start = System.currentTimeMillis();
        isFirstInit = false; // 执行过一次了标记为false
        final ApplicationContext ctx = event.getApplicationContext();
        // 获取parser
        Map<Class<? extends Annotation>, List<LoadtimeMethodAnnotationParser>> parserMap =
                new HashMap<Class<? extends Annotation>, List<LoadtimeMethodAnnotationParser>>();
        for (LoadtimeMethodAnnotationParser parser : ctx.getBeansOfType(LoadtimeMethodAnnotationParser.class).values()) {
            Type[] types = parser.getClass().getGenericInterfaces();
            for (Type t : types) {
                // 获得LoadtimeAnnotationParser的范型参数
                if (t instanceof ParameterizedType && LoadtimeMethodAnnotationParser.class//
                        .isAssignableFrom((Class<?>) (((ParameterizedType) t).getRawType()))) {
                    Type[] args = ((ParameterizedType) t).getActualTypeArguments();
                    if (!parserMap.containsKey(args[0])) {
                        parserMap.put((Class<? extends Annotation>) args[0],
                                new ArrayList<LoadtimeMethodAnnotationParser>());
                    }
                    parserMap.get(args[0]).add(parser);
                }
            }
        }
        // 遍历所有bean,执行这些bean的各个方法上的注解对应的parser
        for (final String name : registry.getBeanDefinitionNames()) {
            try {
                // 遍历一个bean里面所有method上面的注解
                for (final Method method : Class.forName(registry.getBeanDefinition(name).getBeanClassName())
                        .getDeclaredMethods()) {
                    for (Annotation annotation : method.getAnnotations()) {
                        // 如果存在parser
                        if (parserMap.containsKey(annotation.annotationType())) {
                            // prototype类型, 注意
                            if (registry.getBeanDefinition(name).isPrototype()) {
                                logger.info("prototype bean: {} has anntataion {} on method {}", name, annotation,
                                        method);
                            }
                            // 调用各个parser
                            for (LoadtimeMethodAnnotationParser parser : parserMap.get(annotation.annotationType())) {
                                parser.parse(annotation, method, beanFactory, name);
                            }
                        }
                    }
                }
                // 遍历完method
            } catch (ClassNotFoundException e) {
                logger.error("error get bean for class ", e);
            }
        }
        logger.info("load complete spend : {} ms", System.currentTimeMillis() - start);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

}
