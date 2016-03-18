package com.baidu.aenhancer.conf.loadtime;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.entry.Collapse;
import com.baidu.aenhancer.exception.CodingError;
import com.baidu.aenhancer.exception.UnexpectedStateException;

@Component
public class CollapseParser implements LoadtimeMethodAnnotationParser<Collapse> {
    @Autowired
    private SplitParser splitParser;

    private static final Logger logger = LoggerFactory.getLogger(CollapseParser.class);

    Map<String, StatelessCommand> commands = new HashMap<String, StatelessCommand>();

    @Override
    public void parse(Collapse annotation, final Method method, DefaultListableBeanFactory ctx, String beanName) {
        Class<?>[] paramTypes = method.getParameterTypes();
        // 实际上要把List<Object>传给这个方法，paramTypes[0].isAssignableFrom(List.class)
        if (paramTypes.length != 1 || !paramTypes[0].isAssignableFrom(List.class)) {
            throw new CodingError("param of collapse must be a list");
        }

        String commandName = annotation.value();
        if (StringUtils.isEmpty(commandName)) {
            commandName = beanName + SplitParser.COMMAND_SUFFIX;
        }

        if (null != commands.get(commandName)) {
            throw new CodingError("more than one collapse command is named :" + commandName);
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
                    // method.setAccessible(true);
                    return method.invoke(bean, args);
                } catch (Exception e) {
                    throw new UnexpectedStateException(e);
                }
            }
        };

        // register bean
        if (splitParser.commands.containsKey(commandName)) {
            if (ctx.containsBeanDefinition(commandName)) {
                throw new CodingError("bean with name " + commandName + " is already registered");
            }
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SplitProxyImpl.class);
            builder.addPropertyValue("split", splitParser.commands.get(commandName));
            builder.addPropertyValue("collapse", command);
            ctx.registerBeanDefinition(commandName, builder.getBeanDefinition());
            logger.info("SplitProxy with bean name \"{}\" is registerd ", commandName);
            splitParser.commands.remove(commandName);
        } else {
            if (commands.containsKey(commandName)) {
                throw new CodingError("collapse command named :" + commandName + " already exists ");
            }
            commands.put(commandName, command);
        }
    }
}
