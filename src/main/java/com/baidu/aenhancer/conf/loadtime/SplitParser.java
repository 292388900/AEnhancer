package com.baidu.aenhancer.conf.loadtime;

import java.awt.List;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.entry.Split;
import com.baidu.aenhancer.exception.CodingError;
import com.baidu.aenhancer.exception.UnexpectedStateException;

@Component
public class SplitParser implements LoadtimeMethodAnnotationParser<Split> {

    @Autowired
    private CollapseParser collpaseParser;

    Map<String, StatelessCommand> commands = new HashMap<String, StatelessCommand>();

    private static final Logger logger = LoggerFactory.getLogger(CollapseParser.class);

    @Override
    public void parse(Split annotation, final Method split, DefaultListableBeanFactory ctx, String beanName) {
        Class<?> returnVaule = split.getReturnType();
        String commandName = annotation.value();
        if (StringUtils.isEmpty(commandName)) {
            commandName = beanName + ".spliter";
        }

        if (null != commands.get(commandName)) {
            throw new CodingError("more than one split command named :" + commandName);
        }

        if (List.class.isAssignableFrom(returnVaule)) {
            throw new CodingError("the return value of split method: " + commandName + " is not list");
        }

        final Object bean = ctx.getBean(beanName);
        // bean 是无状态的则生成command
        StatelessCommand command = new StatelessCommand() {
            @Override
            public Object call(Object...args) {
                try {
                    // method.setAccessible(true);
                    return split.invoke(bean, args);
                } catch (Exception e) {
                    throw new UnexpectedStateException(e);
                }
            }

            @Override
            public Object getBean() {
                return bean;
            }
        };

        if (collpaseParser.commands.containsKey(commandName)) {
            if (ctx.containsBeanDefinition(commandName)) {
                throw new CodingError("bean with name " + commandName + " is already registered");
            }
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SplitProxyImpl.class);
            builder.addPropertyValue("collapse", collpaseParser.commands.get(commandName));
            builder.addPropertyValue("split", command);
            ctx.registerBeanDefinition(commandName, builder.getBeanDefinition());
            logger.info("SplitProxy with bean name \"{}\" is registerd ", commandName);
            collpaseParser.commands.remove(commandName);
        } else {
            commands.put(commandName, command);
        }
    }
}
