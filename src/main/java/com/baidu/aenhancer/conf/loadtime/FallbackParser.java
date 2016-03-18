package com.baidu.aenhancer.conf.loadtime;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.entry.Fallback;
import com.baidu.aenhancer.exception.CodingError;
import com.baidu.aenhancer.exception.UnexpectedStateException;

@Component
public class FallbackParser implements LoadtimeMethodAnnotationParser<Fallback> {
    private static final Logger logger = LoggerFactory.getLogger(FallbackParser.class);

    @Override
    public void parse(Fallback annotation, final Method method, DefaultListableBeanFactory ctx, String beanName) {
        
        String commandName = annotation.value();
        if (StringUtils.isEmpty(commandName)) {
            commandName = beanName + ".fallback";
        }

        final Object bean = ctx.getBean(beanName);
        StatelessCommand command = new StatelessCommand() {

            @Override
            public Object getBean() {
                return bean;
            }

            @Override
            public Object call(Object...args) {
                try {
                    logger.info("fall back: args: {}, param types: {}", args, method.getParameterTypes().length);
                    if (method.getParameterTypes().length == 0) {
                        return method.invoke(bean);
                    }
                    // method.setAccessible(true);
                    return method.invoke(bean, args);
                } catch (Exception e) {
                    throw new UnexpectedStateException(e);
                }
            }
        };
        // 冲突bean definition
        if (ctx.containsBeanDefinition(commandName)) {
            throw new CodingError("the bean: " + commandName + " already exists");
        }
        // 注册bean
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(FallbackImpl.class);
        builder.addPropertyValue("command", command);
        ctx.registerBeanDefinition(commandName, builder.getBeanDefinition());
        logger.info("FallbakProxy with bean name \"{}\" is registered", commandName);
    }

}
